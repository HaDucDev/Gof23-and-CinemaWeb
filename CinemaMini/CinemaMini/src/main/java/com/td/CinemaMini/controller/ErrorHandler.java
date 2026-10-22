package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import static com.td.CinemaMini.dto.Values.map;

@ControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private final GlobalModelAdvice navigation;

    public ErrorHandler(GlobalModelAdvice navigation) {
        this.navigation = navigation;
    }

    @ExceptionHandler(BusinessException.class)
    public Object business(BusinessException error, HttpServletRequest request) {
        return response(error.getStatus(), error.getCode(), error.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object integrity(DataIntegrityViolationException error, HttpServletRequest request) {
        log.warn("Ràng buộc dữ liệu từ chối thao tác", error);
        return response(
                409,
                "DATA_CONFLICT",
                "Dữ liệu bị trùng hoặc không hợp lệ. Vui lòng tải lại và kiểm tra thông tin.",
                request);
    }

    @ExceptionHandler({PessimisticLockingFailureException.class, QueryTimeoutException.class})
    public Object retry(RuntimeException error, HttpServletRequest request) {
        return response(
                409,
                "RETRY_REQUEST",
                "Dữ liệu đang được xử lý. Vui lòng thử lại với cùng yêu cầu.",
                request);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public Object input(Exception error, HttpServletRequest request) {
        return response(
                400,
                "INVALID_INPUT",
                "Thông tin gửi lên không hợp lệ. Vui lòng tải lại trang.",
                request);
    }

    private Object response(int status, String code, String message, HttpServletRequest request) {
        if (String.valueOf(request.getHeader("Accept")).contains("application/json")) {
            return ResponseEntity.status(status).body(map("code", code, "message", message));
        }
        ModelAndView view = new ModelAndView("error");
        view.setStatus(HttpStatus.valueOf(status));
        // Model của @ExceptionHandler không tự nhận dữ liệu từ @ModelAttribute toàn cục.
        // Bổ sung lại dữ liệu điều hướng để trang lỗi hiển thị đúng tài khoản và vai trò.
        ExtendedModelMap model = new ExtendedModelMap();
        navigation.navigation(request.getUserPrincipal(), model);
        view.addAllObjects(model.asMap());
        view.addObject("errorCode", code);
        view.addObject("message", message);
        return view;
    }
}
