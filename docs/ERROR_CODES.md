"""
ERROR CODES REFERENCE
=====================

Este archivo documenta todos los códigos de error disponibles en la aplicación.
Úsalo como referencia para:
- Documentar APIs
- Implementar manejo de errores en clientes
- Testing de casos de error
- Monitoreo y alertas

"""

# CUSTOMER MODULE
# Status: 400 Bad Request (Business Logic) / 404 Not Found (Resource)

CUSTOMER_NOT_FOUND                  # HTTP 404 - Cliente no encontrado por identificación
CUSTOMER_ALREADY_EXISTS             # HTTP 400 - Cliente ya existe con misma identificación
CUSTOMER_EMAIL_ALREADY_EXISTS       # HTTP 400 - Email ya registrado en otro cliente
CUSTOMER_INACTIVE                   # HTTP 400 - Cliente está inactivo (no puede ser modificado)
CUSTOMER_ALREADY_INACTIVE           # HTTP 400 - Cliente ya está inactivo


# PRODUCT MODULE
# Status: 400 Bad Request (Business Logic) / 404 Not Found (Resource)

PRODUCT_NOT_FOUND                   # HTTP 404 - Producto no encontrado por ID
PRODUCT_UNAVAILABLE                 # HTTP 400 - Producto está unavailable (marcado como no disponible)
PRODUCT_ALREADY_UNAVAILABLE         # HTTP 400 - Producto ya está marcado unavailable
INVALID_STOCK                       # HTTP 400 - Stock negativo o inválido


# ORDER MODULE
# Status: 400 Bad Request (Business Logic) / 404 Not Found (Resource) / 409 Conflict (State)

ORDER_NOT_FOUND                     # HTTP 404 - Orden no encontrada por número
EMPTY_ORDER                         # HTTP 400 - Orden sin items (mínimo 1 requerido)
INSUFFICIENT_STOCK                  # HTTP 400 - Stock insuficiente del producto
INVALID_ORDER_STATUS                # HTTP 409 - Estado inválido para la operación (ej: cancelar)
PRODUCT_UNAVAILABLE_FOR_ORDER       # HTTP 400 - Producto no disponible para ser ordenado


# VALIDATION
# Status: 400 Bad Request

VALIDATION_ERROR                    # HTTP 400 - Error en validación de campos (JSR-303)
INVALID_JSON_FORMAT                 # HTTP 400 - JSON mal formado en request body


# SYSTEM
# Status: 500 Internal Server Error

INTERNAL_SERVER_ERROR               # HTTP 500 - Error interno no esperado


"""
MAPPING TABLE
=============

ErrorCode → HTTP Status → Exception Class

CUSTOMER_NOT_FOUND              → 404 → ResourceNotFoundException
CUSTOMER_ALREADY_EXISTS         → 400 → BusinessException
CUSTOMER_EMAIL_ALREADY_EXISTS   → 400 → BusinessException
CUSTOMER_INACTIVE               → 400 → BusinessException
CUSTOMER_ALREADY_INACTIVE       → 400 → BusinessException

PRODUCT_NOT_FOUND               → 404 → ResourceNotFoundException
PRODUCT_UNAVAILABLE             → 400 → BusinessException
PRODUCT_ALREADY_UNAVAILABLE     → 400 → BusinessException
INVALID_STOCK                   → 400 → BusinessException

ORDER_NOT_FOUND                 → 404 → ResourceNotFoundException
EMPTY_ORDER                     → 400 → BusinessException
INSUFFICIENT_STOCK              → 400 → BusinessException
INVALID_ORDER_STATUS            → 409 → InvalidStateException
PRODUCT_UNAVAILABLE_FOR_ORDER   → 400 → BusinessException

VALIDATION_ERROR                → 400 → MethodArgumentNotValidException (automatic)
INVALID_JSON_FORMAT             → 400 → HttpMessageNotReadableException (automatic)

INTERNAL_SERVER_ERROR           → 500 → Exception (fallback)


"""
EJEMPLOS DE RESPUESTA
=====================

# 404 Not Found - Customer Not Found
GET /api/customers/123

{
  "timestamp": "2025-03-05T10:30:45.123456",
  "status": 404,
  "error": "Customer not found with identification: 123",
  "errorCode": "CUSTOMER_NOT_FOUND",
  "path": "/api/customers/123"
}


# 400 Bad Request - Insufficient Stock
POST /api/orders

{
  "timestamp": "2025-03-05T10:31:15.654321",
  "status": 400,
  "error": "Insufficient stock for product ProductName - Available: 5, Requested: 10",
  "errorCode": "INSUFFICIENT_STOCK",
  "path": "/api/orders"
}


# 409 Conflict - Invalid Order Status
DELETE /api/orders/ORD-00001

{
  "timestamp": "2025-03-05T10:32:00.111111",
  "status": 409,
  "error": "Only orders in CREATED status can be cancelled",
  "errorCode": "INVALID_ORDER_STATUS",
  "path": "/api/orders/ORD-00001"
}


# 400 Bad Request - Validation Error
POST /api/customers

{
  "timestamp": "2025-03-05T10:33:30.222222",
  "status": 400,
  "error": "email: must be a valid email address",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/customers"
}


"""
HOW TO USE IN TESTS
===================

# Testing error code
@Test
public void testCreateCustomerWithDuplicateIdentification() {
    CustomerRequest duplicate = new CustomerRequest(...);
    customerService.saveCustomer(duplicate);  // Primera vez OK
    
    BusinessException ex = assertThrows(BusinessException.class,
        () -> customerService.saveCustomer(duplicate));  // Segunda vez lanza excepción
    
    assertEquals("CUSTOMER_ALREADY_EXISTS", ex.getErrorCode());
    assertEquals("Customer already exists with identification: ...", ex.getMessage());
}


# Testing in REST API
@Test
public void testGetNonExistentCustomer() {
    mockMvc.perform(get("/api/customers/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
            .andExpect(jsonPath("$.status").value(404));
}


"""
HOW TO USE IN CLIENT CODE (JavaScript/TypeScript)
==================================================

// Definir mensajes por código
const ERROR_MESSAGES = {
    'CUSTOMER_NOT_FOUND': 'El cliente no existe en el sistema',
    'CUSTOMER_INACTIVE': 'El cliente está inactivo. Contacte a soporte',
    'INSUFFICIENT_STOCK': 'Stock insuficiente del producto',
    'INVALID_ORDER_STATUS': 'No se puede realizar esta operación en el estado actual',
    'VALIDATION_ERROR': 'Los datos enviados contienen errores',
    'INTERNAL_SERVER_ERROR': 'Error interno. Intente más tarde'
};

// Usar en manejo de errores
async function createOrder(orderData) {
    try {
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            const message = ERROR_MESSAGES[error.errorCode] || error.error;
            showError(message);
            
            // Acciones específicas por error
            switch (error.errorCode) {
                case 'CUSTOMER_INACTIVE':
                    redirectToCustomerReactivation();
                    break;
                case 'INSUFFICIENT_STOCK':
                    suggestAlternativeProduct(error);
                    break;
                case 'VALIDATION_ERROR':
                    highlightInvalidFields(error);
                    break;
            }
        }
    } catch (err) {
        showError('Error de red. Intente nuevamente');
    }
}


"""
INTEGRATION WITH MONITORING/ALERTING
======================================

# Prometheus metrics example
counter_api_errors_total{
    errorCode="INSUFFICIENT_STOCK",
    http_status="400",
    endpoint="/api/orders"
} 25

counter_api_errors_total{
    errorCode="CUSTOMER_NOT_FOUND",
    http_status="404",
    endpoint="/api/customers"
} 142

# Alert rules (Alertmanager)
- alert: HighErrorRate
  expr: rate(counter_api_errors_total[5m]) > 0.1
  annotations:
    summary: "High error rate for {{ $labels.errorCode }}"

# ElasticSearch/Kibana dashboard
field: errorCode
aggregation: cardinality
query: timestamp:[now-24h TO now]
display: Top 10 error codes by frequency


"""
CHANGELOG
=========

v1.0 - Initial Implementation (2025-03-05)
- Added errorCode field to ErrorResponse DTO
- Updated all exception classes with errorCode support
- Updated GlobalExceptionHandler to extract and return error codes
- Updated all service implementations to throw exceptions with error codes
- Created comprehensive documentation
"""
