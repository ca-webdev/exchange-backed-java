package ca.webdev.exchange.web.model;

public class OrderInsertResponse {
    private String orderId;

    private String message;

    public OrderInsertResponse() {
    }

    public OrderInsertResponse(String orderId, String message) {
        this.orderId = orderId;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
