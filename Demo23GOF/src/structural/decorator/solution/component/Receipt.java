package structural.decorator.solution.component;

// Interface khai báo print(), được hóa đơn cơ bản và các lớp bọc cùng triển khai (Component).
public interface Receipt {
    // In các phần của hóa đơn qua object hóa đơn hoặc lớp bọc cụ thể.
    void print();
}
