package behavioral.templatemethod.solution.generator;

// Class con cho Excel: dùng cùng khung quy trình và thay bước tạo định dạng.
public class ExcelReportGenerator extends ReportGenerator {
    @Override
    protected void createFormat() {
        System.out.println("3. [EXCEL] Đang tạo định dạng Excel");
    }
}
