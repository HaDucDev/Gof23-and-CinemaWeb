package behavioral.templatemethod.solution.generator;

// Class con cho PDF: kế thừa quy trình, chỉ cung cấp cách tạo định dạng PDF.
public class PdfReportGenerator extends ReportGenerator {
    @Override
    protected void createFormat() {
        System.out.println("3. [PDF] Đang tạo định dạng PDF");
    }
}
