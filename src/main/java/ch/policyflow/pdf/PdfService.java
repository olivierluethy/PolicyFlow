package ch.policyflow.pdf;

import ch.policyflow.domain.entity.Offer;
import ch.policyflow.domain.entity.Policy;
import ch.policyflow.domain.enums.Franchise;
import ch.policyflow.service.OfferService;
import ch.policyflow.service.PolicyService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Renders branded PDF documents (offers and policies) using OpenPDF.
 *
 * <p>Documents follow the PolicyFlow visual identity: a red wordmark header, a clean
 * two-column key/value layout for details, and a clear premium breakdown.</p>
 */
@ApplicationScoped
public class PdfService {

    private static final Color SWISS_RED = new Color(0xDC, 0x26, 0x26); // red-600
    private static final Color INK = new Color(0x11, 0x18, 0x27);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color LINE = new Color(0xE5, 0xE7, 0xEB);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Inject
    OfferService offerService;

    @Inject
    PolicyService policyService;

    /**
     * Generates a PDF for an offer.
     *
     * @param offerId the offer id
     * @return the rendered PDF as a byte array
     */
    public byte[] generateOfferPdf(Long offerId) {
        Offer offer = offerService.get(offerId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        PdfWriter.getInstance(doc, out);
        doc.open();

        header(doc, "OFFERTE", "Offer #" + offer.id);

        PdfPTable details = twoColumnTable();
        addRow(details, "Customer", offer.customer.firstName + " " + offer.customer.lastName);
        addRow(details, "Email", offer.customer.email);
        addRow(details, "Provider", offer.provider.name + " (" + offer.provider.code + ")");
        addRow(details, "Canton", offer.canton.name() + " — " + offer.canton.getFullName());
        addRow(details, "Age", String.valueOf(offer.age));
        addRow(details, "Franchise", "CHF " + offer.franchise);
        addRow(details, "Accident cover", offer.unfalleinschluss ? "Included" : "Not included");
        addRow(details, "Status", offer.status.name());
        addRow(details, "Valid until", offer.validUntil == null ? "—" : offer.validUntil.format(DATE));
        doc.add(details);

        doc.add(spacer());
        premiumBox(doc, offer.monthlyPremium, offer.yearlyPremium);

        if (offer.notes != null && !offer.notes.isBlank()) {
            doc.add(spacer());
            doc.add(label("Notes"));
            doc.add(new Paragraph(offer.notes, body()));
        }

        footer(doc);
        doc.close();
        return out.toByteArray();
    }

    /**
     * Generates a PDF for an issued policy.
     *
     * @param policyId the policy id
     * @return the rendered PDF as a byte array
     */
    public byte[] generatePolicyPdf(Long policyId) {
        Policy policy = policyService.get(policyId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        PdfWriter.getInstance(doc, out);
        doc.open();

        header(doc, "POLICE", policy.policyNumber);

        PdfPTable details = twoColumnTable();
        addRow(details, "Policy number", policy.policyNumber);
        addRow(details, "Customer", policy.customer.firstName + " " + policy.customer.lastName);
        addRow(details, "Email", policy.customer.email);
        addRow(details, "Provider", policy.provider.name + " (" + policy.provider.code + ")");
        addRow(details, "Deductible", "CHF " + safeFranchise(policy.offer.franchise));
        addRow(details, "Accident cover", policy.offer.unfalleinschluss ? "Included" : "Not included");
        addRow(details, "Status", policy.status.name());
        addRow(details, "Start date", policy.startDate.format(DATE));
        addRow(details, "End date", policy.endDate == null ? "—" : policy.endDate.format(DATE));
        doc.add(details);

        doc.add(spacer());
        BigDecimal yearly = policy.monthlyPremium.multiply(BigDecimal.valueOf(12));
        premiumBox(doc, policy.monthlyPremium, yearly);

        footer(doc);
        doc.close();
        return out.toByteArray();
    }

    // ---- shared rendering helpers -------------------------------------------------

    private void header(Document doc, String docType, String reference) {
        Paragraph brand = new Paragraph();
        brand.add(new Phrase("Policy", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, INK)));
        brand.add(new Phrase("Flow", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, SWISS_RED)));
        doc.add(brand);

        Paragraph sub = new Paragraph("Swiss Insurance Broker Platform",
                FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
        sub.setSpacingAfter(16);
        doc.add(sub);

        Paragraph title = new Paragraph(docType,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, SWISS_RED));
        doc.add(title);
        Paragraph ref = new Paragraph(reference + "  ·  Issued " + LocalDate.now().format(DATE),
                FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
        ref.setSpacingAfter(18);
        doc.add(ref);
    }

    private PdfPTable twoColumnTable() {
        PdfPTable table = new PdfPTable(new float[]{1f, 2f});
        table.setWidthPercentage(100);
        return table;
    }

    private void addRow(PdfPTable table, String key, String value) {
        PdfPCell k = new PdfPCell(new Phrase(key,
                FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED)));
        PdfPCell v = new PdfPCell(new Phrase(value,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, INK)));
        for (PdfPCell c : new PdfPCell[]{k, v}) {
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(LINE);
            c.setPadding(6);
        }
        table.addCell(k);
        table.addCell(v);
    }

    private void premiumBox(Document doc, BigDecimal monthly, BigDecimal yearly) {
        PdfPTable box = new PdfPTable(2);
        box.setWidthPercentage(100);

        box.addCell(premiumCell("Monthly premium", "CHF " + monthly));
        box.addCell(premiumCell("Yearly premium", "CHF " + yearly));
        try {
            doc.add(box);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render premium box", e);
        }
    }

    private PdfPCell premiumCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(0xFE, 0xF2, 0xF2)); // red-50
        cell.setBorderColor(SWISS_RED);
        cell.setPadding(14);
        Paragraph l = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
        Paragraph v = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, SWISS_RED));
        cell.addElement(l);
        cell.addElement(v);
        return cell;
    }

    private Paragraph spacer() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph label(String text) {
        return new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, MUTED));
    }

    private Font body() {
        return FontFactory.getFont(FontFactory.HELVETICA, 10, INK);
    }

    private void footer(Document doc) {
        Paragraph footer = new Paragraph(
                "This document was generated by PolicyFlow and is for informational purposes only. "
                        + "Premiums are indicative and subject to the provider's terms.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, MUTED));
        footer.setSpacingBefore(28);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private String safeFranchise(int amount) {
        try {
            return String.valueOf(Franchise.fromAmount(amount).getAmount());
        } catch (IllegalArgumentException e) {
            return String.valueOf(amount);
        }
    }
}
