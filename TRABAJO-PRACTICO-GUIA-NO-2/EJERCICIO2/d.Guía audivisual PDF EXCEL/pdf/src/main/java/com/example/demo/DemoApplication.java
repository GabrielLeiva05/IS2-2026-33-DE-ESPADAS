package com.example.demo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


@SpringBootApplication
public class DemoApplication{

	public static void main(String[] args) throws Exception{
		SpringApplication.run(DemoApplication.class, args);
		Document doc = new Document();

		PdfWriter.getInstance(doc, new FileOutputStream("sample.pdf"));
		doc.open();
		Paragraph par = new Paragraph("Lorem ipsum some text");
		doc.add(par);

		PdfPTable table = new PdfPTable(3);
		tableHeader(table);
		addRow(table);
		addCustomRow(table);
		doc.add(table);
		doc.close();


	}
	private static void tableHeader(PdfPTable table){
		Stream.of("Id", "First Name", "Last Name").forEach( title -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.CYAN);
			header.setPhrase(new Phrase(title));
			table.addCell(header);
		});
	}


	private static void addRow(PdfPTable table){
		table.addCell("1");
		table.addCell("PDF");
		table.addCell("Generation");
	}

	private static void addCustomRow(PdfPTable table) throws URISyntaxException, BadElementException, IOException {
		Path path = Paths.get(ClassLoader.getSystemResource("p3.jpg").toURI());
		Image img = Image.getInstance(path.toAbsolutePath().toString());
		img.scalePercent(20);

		PdfPCell imageCell = new PdfPCell(img);
		table.addCell(imageCell);
		PdfPCell desc = new PdfPCell(new Phrase("Description"));
		desc.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(desc);

		PdfPCell remarks = new PdfPCell(new Phrase("Remarks"));
		remarks.setVerticalAlignment(Element.ALIGN_CENTER);
		table.addCell(remarks);
	}
}
