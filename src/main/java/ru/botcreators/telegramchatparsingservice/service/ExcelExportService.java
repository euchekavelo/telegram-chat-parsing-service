package ru.botcreators.telegramchatparsingservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.ChannelRecord;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final UserKeyService userKeyService;

    public static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final List<String> USER_HEADERS = List.of(
            "Дата экспорта",
            "Username",
            "Имя и фамилия",
            "Описание",
            "Дата регистрации",
            "Наличие канала в профиле"
    );

    private static final List<String> CHANNEL_HEADERS = List.of(
            "Дата экспорта",
            "Username",
            "Название",
            "URL",
            "Откуда найден"
    );

    private static final ZoneId EXPORT_ZONE = ZoneId.systemDefault();

    public ExportedFile buildXlsx(ExportPayload payload, String filenamePrefix) {
        Objects.requireNonNull(payload, "payload");
        String prefix = (filenamePrefix == null || filenamePrefix.isBlank()) ? "chat_export" : filenamePrefix;

        Instant exportInstant = payload.getExportInstant() != null ? payload.getExportInstant() : Instant.now();
        Date exportDate = Date.from(exportInstant);

        List<UserRecord> participants = dedupeUsers(payload.getParticipants());
        List<UserRecord> mentions = dedupeUsers(payload.getMentions());
        List<ChannelRecord> channels = dedupeChannels(payload.getChannels());

        try (Workbook wb = new XSSFWorkbook()) {

            CellStyle headerStyle = headerStyle(wb);
            CellStyle dateTimeStyle = dateTimeStyle(wb);
            CellStyle wrapTopStyle = wrapTopStyle(wb);
            CellStyle plainTopStyle = plainTopStyle(wb);

            Sheet sParticipants = wb.createSheet("participants");
            Sheet sMentions = wb.createSheet("mentions");
            Sheet sChannels = wb.createSheet("channels");

            writeHeader(sParticipants, USER_HEADERS, headerStyle);
            writeHeader(sMentions, USER_HEADERS, headerStyle);
            writeHeader(sChannels, CHANNEL_HEADERS, headerStyle);

            writeUsers(sParticipants, exportDate, participants, dateTimeStyle, wrapTopStyle, plainTopStyle);
            writeUsers(sMentions, exportDate, mentions, dateTimeStyle, wrapTopStyle, plainTopStyle);
            writeChannels(sChannels, exportDate, channels, dateTimeStyle, wrapTopStyle);

            applyAutoFilter(sParticipants, USER_HEADERS.size());
            applyAutoFilter(sMentions, USER_HEADERS.size());
            applyAutoFilter(sChannels, CHANNEL_HEADERS.size());

            finalizeSheet(sParticipants, USER_HEADERS.size());
            finalizeSheet(sMentions, USER_HEADERS.size());
            finalizeSheet(sChannels, CHANNEL_HEADERS.size());

            String fileName = prefix + "_" + exportInstant
                    .atZone(EXPORT_ZONE)
                    .toLocalDateTime()
                    .toString()
                    .replace(":", "-")
                    .replace("T", "_") + ".xlsx";

            byte[] bytes = toBytes(wb);
            return new ExportedFile(fileName, CONTENT_TYPE, bytes);

        } catch (IOException e) {
            throw new RuntimeException("Failed to build XLSX", e);
        }
    }

    private void writeUsers(Sheet sheet,
                            Date exportDate,
                            List<UserRecord> users,
                            CellStyle exportDateStyle,
                            CellStyle wrapTopStyle,
                            CellStyle plainTopStyle) {

        int rowIdx = 1;
        for (UserRecord u : users) {
            Row r = sheet.createRow(rowIdx++);
            r.setHeightInPoints(18);

            int c = 0;

            Cell c0 = r.createCell(c++);
            c0.setCellValue(exportDate);
            c0.setCellStyle(exportDateStyle);

            r.createCell(c++).setCellValue(formatUsername(u.getUsername()));

            Cell name = r.createCell(c++);
            name.setCellValue(nullToEmpty(u.getFullName()));
            name.setCellStyle(plainTopStyle);

            Cell bio = r.createCell(c++);
            bio.setCellValue(nullToEmpty(u.getBio()));
            bio.setCellStyle(wrapTopStyle);

            r.createCell(c++).setCellValue(u.getRegistrationDate() != null ? u.getRegistrationDate().toString() : "");
            r.createCell(c).setCellValue(formatBool(u.getHasChannelInProfile()));
        }
    }

    private void writeChannels(Sheet sheet,
                               Date exportDate,
                               List<ChannelRecord> channels,
                               CellStyle exportDateStyle,
                               CellStyle wrapTopStyle) {

        CreationHelper helper = sheet.getWorkbook().getCreationHelper();

        int rowIdx = 1;
        for (ChannelRecord ch : channels) {
            Row r = sheet.createRow(rowIdx++);

            int c = 0;

            Cell c0 = r.createCell(c++);
            c0.setCellValue(exportDate);
            c0.setCellStyle(exportDateStyle);

            r.createCell(c++).setCellValue(formatUsername(ch.getUsername()));
            r.createCell(c++).setCellValue(nullToEmpty(ch.getTitle()));

            String url = nullToEmpty(ch.getUrl());
            Cell urlCell = r.createCell(c++);
            urlCell.setCellValue(url);
            urlCell.setCellStyle(wrapTopStyle);

            if (!url.isBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
                link.setAddress(url);
                urlCell.setHyperlink(link);
            }

            r.createCell(c).setCellValue(nullToEmpty(ch.getDiscoveredFrom()));
        }
    }

    private void writeHeader(Sheet sheet, List<String> headers, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
    }

    private void applyAutoFilter(Sheet sheet, int columnCount) {
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, Math.max(0, columnCount - 1)));
    }

    private void finalizeSheet(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) sheet.autoSizeColumn(i);

        int max = 55 * 256;
        for (int i = 0; i < columnCount; i++) {
            int w = sheet.getColumnWidth(i);
            if (w > max) sheet.setColumnWidth(i, max);
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle dateTimeStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        style.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm:ss"));
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle wrapTopStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle plainTopStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private String formatUsername(String username) {
        if (username == null || username.isBlank()) return "";
        String u = username.trim();
        if (u.startsWith("@")) return u;
        return "@" + u;
    }

    private String formatBool(Boolean v) {
        if (v == null) return "";
        return v ? "Да" : "Нет";
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private List<UserRecord> dedupeUsers(List<UserRecord> users) {
        if (users == null) return List.of();

        Map<String, UserRecord> map = new LinkedHashMap<>();
        for (UserRecord u : users) {
            if (u == null) continue;
            map.putIfAbsent(userKeyService.key(u), u);
        }

        return map.values().stream()
                .sorted(Comparator
                        .comparing((UserRecord u) -> Optional.ofNullable(u.getUsername()).orElse(""))
                        .thenComparing(u -> Optional.ofNullable(u.getFullName()).orElse("")))
                .collect(Collectors.toList());
    }

    private List<ChannelRecord> dedupeChannels(List<ChannelRecord> channels) {
        if (channels == null) return List.of();

        Map<String, ChannelRecord> map = new LinkedHashMap<>();
        for (ChannelRecord ch : channels) {
            if (ch == null) continue;
            String key = channelKey(ch);
            map.putIfAbsent(key, ch);
        }

        return map.values().stream()
                .sorted(Comparator.comparing((ChannelRecord c) -> Optional.ofNullable(c.getUsername()).orElse(""))
                        .thenComparing(c -> Optional.ofNullable(c.getTitle()).orElse("")))
                .collect(Collectors.toList());
    }

    private String channelKey(ChannelRecord ch) {
        String username = ch.getUsername();
        if (username != null && !username.isBlank()) {
            return "c:" + username.trim().toLowerCase().replaceFirst("^@", "");
        }
        String title = Optional.ofNullable(ch.getTitle()).orElse("").trim().toLowerCase();
        String url = Optional.ofNullable(ch.getUrl()).orElse("").trim().toLowerCase();
        return "t:" + title + "|" + url;
    }
}
