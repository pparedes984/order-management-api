package com.example.ordersmanagement.util;

import org.springframework.stereotype.Component;

@Component
public class CommonUtil {

    /**
     * Obtiene el nombre del método llamador para enriquecer los logs.
     *
     * <p>Nota: este enfoque tiene un costo adicional; puede reemplazarse por nombres estáticos
     * o por trazas del logger si se requiere mayor performance.</p>
     *
     * @return nombre del método llamador o "unknown" si no se pudo determinar
     */
    public static String getCurrentMethodName() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames ->
                        frames
                                .skip(1) // 👈 saltamos este método
                                .findFirst()
                                .map(StackWalker.StackFrame::getMethodName)
                                .orElse("unknown")
                );
    }
}
