package com.tarkshastra.app.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final AggregationService aggregationService;
    private final InterventionRepository interventionRepository;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 51, 102));
    private static final Color HIGH_RISK_COLOR = new Color(220, 53, 69);
    private static final Color MEDIUM_RISK_COLOR = new Color(255, 193, 7);
    private static final Color LOW_RISK_COLOR = new Color(40, 167, 69);

    public byte[] generateStudentRiskReport(UUID instituteId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("Student Risk Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), BODY_FONT);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 1.5f, 2f, 1.5f, 1.2f, 1.2f, 1.2f});

            addHeaderCell(table, "Student Name");
            addHeaderCell(table, "Roll No");
            addHeaderCell(table, "Department");
            addHeaderCell(table, "Class");
            addHeaderCell(table, "Risk Score");
            addHeaderCell(table, "Risk Level");
            addHeaderCell(table, "Mentor");

            List<StudentProfile> students = studentProfileRepository.findByInstituteId(instituteId,
                    org.springframework.data.domain.Pageable.unpaged()).getContent();

            for (StudentProfile sp : students) {
                RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                        sp.getUser().getId()).orElse(null);

                addBodyCell(table, sp.getFullName());
                addBodyCell(table, sp.getRollNumber());
                addBodyCell(table, sp.getDepartment().getName());
                addBodyCell(table, sp.getClassEntity().getName());
                addBodyCell(table, risk != null ? String.format("%.1f", risk.getRiskScore()) : "N/A");

                PdfPCell riskCell = new PdfPCell(new Phrase(
                        risk != null ? risk.getRiskLabel().name() : "N/A", BODY_FONT));
                riskCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                riskCell.setPadding(5);
                if (risk != null) {
                    riskCell.setBackgroundColor(getRiskColor(risk.getRiskLabel()));
                }
                table.addCell(riskCell);

                addBodyCell(table, sp.getMentor() != null ? sp.getMentor().getFullName() : "Unassigned");
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            log.error("Failed to generate risk report PDF: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }

        return baos.toByteArray();
    }

    public byte[] generateStudentDetailReport(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("Student Detail Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Student info
            document.add(new Paragraph("Student Information", SECTION_FONT));
            document.add(new Paragraph("Name: " + sp.getFullName(), BODY_FONT));
            document.add(new Paragraph("Roll Number: " + sp.getRollNumber(), BODY_FONT));
            document.add(new Paragraph("Department: " + sp.getDepartment().getName(), BODY_FONT));
            document.add(new Paragraph("Class: " + sp.getClassEntity().getName(), BODY_FONT));
            document.add(new Paragraph("Semester: " + sp.getSemester(), BODY_FONT));
            document.add(new Paragraph("Mentor: " +
                    (sp.getMentor() != null ? sp.getMentor().getFullName() : "Unassigned"), BODY_FONT));
            document.add(Chunk.NEWLINE);

            // Overall Risk
            RiskScore overall = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                    .orElse(null);
            document.add(new Paragraph("Overall Risk Assessment", SECTION_FONT));
            if (overall != null) {
                document.add(new Paragraph("Risk Score: " + String.format("%.1f", overall.getRiskScore()), BODY_FONT));
                document.add(new Paragraph("Risk Label: " + overall.getRiskLabel().name(), BODY_FONT));
                document.add(new Paragraph("Attendance Score: " +
                        (overall.getAttendanceScore() != null ? String.format("%.1f", overall.getAttendanceScore()) : "N/A"), BODY_FONT));
                document.add(new Paragraph("Marks Score: " +
                        (overall.getMarksScore() != null ? String.format("%.1f", overall.getMarksScore()) : "N/A"), BODY_FONT));
                document.add(new Paragraph("Assignment Score: " +
                        (overall.getAssignmentScore() != null ? String.format("%.1f", overall.getAssignmentScore()) : "N/A"), BODY_FONT));
                document.add(new Paragraph("LMS Score: " +
                        (overall.getLmsScore() != null ? String.format("%.1f", overall.getLmsScore()) : "N/A"), BODY_FONT));
            } else {
                document.add(new Paragraph("No risk data available.", BODY_FONT));
            }
            document.add(Chunk.NEWLINE);

            // Subject-wise breakdown
            document.add(new Paragraph("Subject-Wise Academic Data", SECTION_FONT));
            List<SubjectClassMapping> mappings = subjectClassMappingRepository.findByClassEntityId(
                    sp.getClassEntity().getId());

            PdfPTable subjTable = new PdfPTable(5);
            subjTable.setWidthPercentage(100);
            addHeaderCell(subjTable, "Subject");
            addHeaderCell(subjTable, "Attendance %");
            addHeaderCell(subjTable, "Avg IA Marks");
            addHeaderCell(subjTable, "Assignment %");
            addHeaderCell(subjTable, "LMS Score");

            for (SubjectClassMapping m : mappings) {
                UUID subId = m.getSubject().getId();
                addBodyCell(subjTable, m.getSubject().getName());
                Double att = aggregationService.getAttendanceScore(studentId, subId);
                Double mk = aggregationService.getMarksScore(studentId, subId);
                Double asg = aggregationService.getAssignmentScore(studentId, subId);
                Double lms = aggregationService.getLmsScore(studentId, subId);
                addBodyCell(subjTable, att != null ? String.format("%.1f", att) : "N/A");
                addBodyCell(subjTable, mk != null ? String.format("%.1f", mk) : "N/A");
                addBodyCell(subjTable, asg != null ? String.format("%.1f", asg) : "N/A");
                addBodyCell(subjTable, lms != null ? String.format("%.1f", lms) : "N/A");
            }
            document.add(subjTable);
            document.add(Chunk.NEWLINE);

            // Interventions
            List<Intervention> interventions = interventionRepository.findByStudentId(studentId);
            if (!interventions.isEmpty()) {
                document.add(new Paragraph("Intervention History", SECTION_FONT));
                for (Intervention intr : interventions) {
                    document.add(new Paragraph("• " + intr.getInterventionDate() + " — " +
                            intr.getInterventionType().name() + ": " + intr.getRemarks(), BODY_FONT));
                }
            }

            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), BODY_FONT);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            log.error("Failed to generate student detail PDF: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }

        return baos.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(0, 51, 102));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private Color getRiskColor(RiskLabel label) {
        return switch (label) {
            case HIGH -> HIGH_RISK_COLOR;
            case MEDIUM -> MEDIUM_RISK_COLOR;
            case LOW -> LOW_RISK_COLOR;
        };
    }
}
