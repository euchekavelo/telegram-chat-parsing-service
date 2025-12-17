package ru.botcreators.telegramchatparsingservice.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import ru.botcreators.telegramchatparsingservice.model.ChannelRecord;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.service.impl.ExcelExportServiceImpl;
import ru.botcreators.telegramchatparsingservice.service.impl.UserKeyServiceImpl;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceImplTest {

    private final UserKeyService keyService = new UserKeyServiceImpl();
    private final ExcelExportService service = new ExcelExportServiceImpl(keyService);

    @Test
    void buildXlsx_shouldCreateThreeSheetsWithExpectedNames() throws Exception {
        ExportPayload payload = new ExportPayload();
        payload.setExportInstant(Instant.parse("2025-12-15T00:00:00Z"));
        payload.setParticipants(List.of(user("1", "user1", "User One")));
        payload.setMentions(List.of(user("2", "user2", "User Two")));
        payload.setChannels(List.of(new ChannelRecord("chan", "Chan", "https://t.me/chan", "mention")));

        ExportedFile file = service.buildXlsx(payload, "my_chat");
        assertNotNull(file);
        assertTrue(file.getFilename().startsWith("my_chat_"));
        assertTrue(file.getFilename().endsWith(".xlsx"));
        assertNotNull(file.getBytes());
        assertTrue(file.getBytes().length > 0);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            assertNotNull(wb.getSheet("participants"));
            assertNotNull(wb.getSheet("mentions"));
            assertNotNull(wb.getSheet("channels"));
        }
    }

    @Test
    void buildXlsx_shouldWriteHeaders() throws Exception {
        ExportPayload payload = new ExportPayload();
        payload.setParticipants(List.of(user("1", "user1", "User One")));

        ExportedFile file = service.buildXlsx(payload, "t");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            Sheet s = wb.getSheet("participants");
            Row header = s.getRow(0);
            assertEquals("Дата экспорта", header.getCell(0).getStringCellValue());
            assertEquals("Username", header.getCell(1).getStringCellValue());
            assertEquals("Имя и фамилия", header.getCell(2).getStringCellValue());
        }
    }

    @Test
    void buildXlsx_shouldDedupeUsersWithinSheet_byUserId() throws Exception {
        UserRecord a = user("1", "user1", "User One");
        UserRecord a2 = user("1", "user1_dup", "User One Dup");

        ExportPayload payload = new ExportPayload();
        payload.setParticipants(List.of(a, a2));

        ExportedFile file = service.buildXlsx(payload, "t");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            Sheet s = wb.getSheet("participants");

            assertNotNull(s.getRow(1));
            assertNull(s.getRow(2));
        }
    }

    @Test
    void buildXlsx_shouldCreateHyperlinkForChannelUrl() throws Exception {
        ExportPayload payload = new ExportPayload();
        payload.setChannels(List.of(new ChannelRecord("chan", "Chan", "https://t.me/chan", "mention")));

        ExportedFile file = service.buildXlsx(payload, "t");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            Sheet s = wb.getSheet("channels");
            Row r = s.getRow(1);
            assertNotNull(r);

            Cell urlCell = r.getCell(3);
            assertEquals("https://t.me/chan", urlCell.getStringCellValue());
            assertNotNull(urlCell.getHyperlink());
            assertEquals("https://t.me/chan", urlCell.getHyperlink().getAddress());
        }
    }

    @Test
    void buildXlsx_shouldWriteExportDateAsNumericDateCell() throws Exception {
        ExportPayload payload = new ExportPayload();
        payload.setExportInstant(Instant.parse("2025-12-15T00:00:00Z"));
        payload.setParticipants(List.of(user("1", "user1", "User One")));

        ExportedFile file = service.buildXlsx(payload, "t");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            Sheet s = wb.getSheet("participants");
            Row r = s.getRow(1);

            Cell dateCell = r.getCell(0);

            assertEquals(CellType.NUMERIC, dateCell.getCellType());
            assertTrue(DateUtil.isCellDateFormatted(dateCell));
        }
    }

    private static UserRecord user(String id, String username, String name) {
        UserRecord u = new UserRecord();
        u.setUserId(id);
        u.setUsername(username);
        u.setFullName(name);
        return u;
    }
}
