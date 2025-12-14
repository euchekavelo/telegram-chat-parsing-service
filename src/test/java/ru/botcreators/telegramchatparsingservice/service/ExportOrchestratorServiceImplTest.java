package ru.botcreators.telegramchatparsingservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;
import ru.botcreators.telegramchatparsingservice.model.ExportResponseType;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.service.impl.ExportOrchestratorServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportOrchestratorServiceImplTest {

    @Test
    void export_shouldReturnText_whenTotalIs50() {
        UserAggregationService agg = mock(UserAggregationService.class);
        TextExportService text = mock(TextExportService.class);
        ExcelExportService excel = mock(ExcelExportService.class);

        when(agg.collectUniqueUsers(any()))
                .thenReturn(buildUsers(49));
        when(text.buildText(any()))
                .thenReturn("TEXT");

        ExportOrchestratorServiceImpl svc = new ExportOrchestratorServiceImpl(agg, text, excel);

        ExportResponse resp = svc.export(new ExportPayload(), "p");

        assertEquals(ExportResponseType.TEXT, resp.getType());
        assertEquals("TEXT", resp.getText());

        verify(text, times(1)).buildText(any());
        verify(excel, never()).buildXlsx(any(), anyString());
    }

    @Test
    void export_shouldReturnExcel_whenTotalIs51() {
        UserAggregationService agg = mock(UserAggregationService.class);
        TextExportService text = mock(TextExportService.class);
        ExcelExportService excel = mock(ExcelExportService.class);

        when(agg.collectUniqueUsers(any()))
                .thenReturn(buildUsers(51));

        ExportedFile file = new ExportedFile("f.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3});
        when(excel.buildXlsx(any(), anyString())).thenReturn(file);

        ExportOrchestratorServiceImpl svc = new ExportOrchestratorServiceImpl(agg, text, excel);

        ExportResponse resp = svc.export(new ExportPayload(), "p");

        assertEquals(ExportResponseType.EXCEL, resp.getType());
        assertNotNull(resp.getFile());
        assertEquals("f.xlsx", resp.getFile().getFilename());

        verify(excel, times(1)).buildXlsx(any(), anyString());
        verify(text, never()).buildText(any());
    }

    private static List<UserRecord> buildUsers(int n) {
        return java.util.stream.IntStream.rangeClosed(1, n)
                .mapToObj(i -> {
                    UserRecord u = new UserRecord();
                    u.setUserId(String.valueOf(i));
                    u.setUsername("user" + i);
                    return u;
                })
                .toList();
    }
}
