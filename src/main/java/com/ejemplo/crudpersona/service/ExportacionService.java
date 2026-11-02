package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.dto.PersonaDto;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Service
public class ExportacionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] ENCABEZADOS = {"ID", "Nombre", "Apellido", "DNI", "Correo", "Telefono"};

    private static final List<Function<PersonaDto, String>> VALORES = List.of(
            p -> String.valueOf(p.getId()),
            PersonaDto::getNombre,
            PersonaDto::getApellido,
            PersonaDto::getDni,
            p -> textoODefecto(p.getEmail()),
            p -> textoODefecto(p.getTelefono()));

    public void aExcel(List<PersonaDto> personas, Path destino) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); OutputStream salida = Files.newOutputStream(destino)) {
            Sheet hoja = libro.createSheet("Personas");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(negrita);
            estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row encabezado = hoja.createRow(0);
            for (int c = 0; c < ENCABEZADOS.length; c++) {
                Cell celda = encabezado.createCell(c);
                celda.setCellValue(ENCABEZADOS[c]);
                celda.setCellStyle(estiloEncabezado);
            }

            for (int f = 0; f < personas.size(); f++) {
                Row fila = hoja.createRow(f + 1);
                PersonaDto p = personas.get(f);
                fila.createCell(0).setCellValue(p.getId());
                for (int c = 1; c < VALORES.size(); c++) {
                    fila.createCell(c).setCellValue(VALORES.get(c).apply(p));
                }
            }

            for (int c = 0; c < ENCABEZADOS.length; c++) {
                hoja.autoSizeColumn(c);
            }
            libro.write(salida);
        }
    }

    public void aPdf(List<PersonaDto> personas, Path destino) throws IOException {
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(new Paragraph("Reporte de Personas",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            documento.add(new Paragraph("Generado el " + LocalDateTime.now().format(FECHA)
                    + "  |  " + personas.size() + " registro(s)",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY)));
            documento.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(new float[]{1f, 3f, 3f, 2f, 4f, 2f});
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);

            com.lowagie.text.Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (String titulo : ENCABEZADOS) {
                PdfPCell celda = new PdfPCell(new Phrase(titulo, fuenteEncabezado));
                celda.setBackgroundColor(new Color(9, 105, 218));
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            com.lowagie.text.Font fuenteDato = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (PersonaDto p : personas) {
                for (Function<PersonaDto, String> valor : VALORES) {
                    PdfPCell celda = new PdfPCell(new Phrase(valor.apply(p), fuenteDato));
                    celda.setPadding(4);
                    tabla.addCell(celda);
                }
            }

            documento.add(tabla);
            // Se cierra antes que el flujo de salida para que termine de escribir el archivo
            documento.close();
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    private static String textoODefecto(String valor) {
        return valor == null ? "" : valor;
    }
}
