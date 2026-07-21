package edu.eci.arsw.blueprints.dto;

/**
 * Envoltura uniforme de respuesta para toda la API (Actividad 3).
 *
 * <pre>
 * {
 *   "code": 200,
 *   "message": "execute ok",
 *   "data": { ... }
 * }
 * </pre>
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "execute ok", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "created", data);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(202, "accepted", data);
    }
}
