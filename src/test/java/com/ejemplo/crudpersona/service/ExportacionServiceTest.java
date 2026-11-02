package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.dto.PersonaDto;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportacionServiceTest {

    private final ExportacionService exportacion = new ExportacionService();

    private final List<PersonaDto> personas = List.of(
            PersonaDto.builder().id(1L).nombre("Ana").apellido("Torres").dni("11111111")
                    .email("ana@correo.com").telefono("987654321").build(),
            PersonaDto.builder().id(2L).nombre("Luis").apellido("Quispe").dni("22222222").build());

    @Test
    void excelTieneEncabezadoYUnaFilaPorPersona(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("personas.xlsx");

        exportacion.aExcel(personas, archivo);

        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            Sheet hoja = libro.getSheetAt(0);
            assertThat(hoja.getLastRowNum()).isEqualTo(2);
            assertThat(hoja.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Nombre");
            assertThat(hoja.getRow(1).getCell(3).getStringCellValue()).isEqualTo("11111111");
            assertThat(hoja.getRow(2).getCell(4).getStringCellValue()).isEmpty();
        }
    }

    @Test
    void pdfSeGeneraCorrectamente(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("personas.pdf");

        exportacion.aPdf(personas, archivo);

        byte[] contenido = Files.readAllBytes(archivo);
        assertThat(new String(contenido, 0, 5)).isEqualTo("%PDF-");
        assertThat(contenido.length).isGreaterThan(1000);
    }
}
