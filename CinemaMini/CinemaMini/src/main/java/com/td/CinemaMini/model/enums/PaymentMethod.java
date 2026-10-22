package com.td.CinemaMini.model.enums;

/** Các mã phương thức thanh toán và hoàn tiền được hỗ trợ qua các adapter mô phỏng local. */
public enum PaymentMethod {
    /** Tiền mặt, được mô phỏng qua CashPaymentAdapter. */
    CASH("Tiền mặt"),
    /** Chuyển khoản ngân hàng, được mô phỏng qua BankPaymentAdapter. */
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    /** Thẻ ATM nội địa, được mô phỏng qua BankPaymentAdapter. */
    DOMESTIC_ATM("Thẻ ATM nội địa"),
    /** Thẻ Visa, được mô phỏng qua CardPaymentAdapter. */
    VISA("Visa"),
    /** Thẻ Mastercard, được mô phỏng qua CardPaymentAdapter. */
    MASTERCARD("Mastercard"),
    /** Ví MoMo, được mô phỏng qua WalletPaymentAdapter. */
    MOMO("MoMo"),
    /** Ví ZaloPay, được mô phỏng qua WalletPaymentAdapter. */
    ZALOPAY("ZaloPay"),
    /** VNPay; code hiện tại dùng WalletPaymentAdapter để mô phỏng. */
    VNPAY("VNPay");
    /** Tên phương thức thanh toán hiển thị cho khách trên giao diện. */
    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
