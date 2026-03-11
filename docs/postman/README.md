# Postman - Datos de prueba reales

## Archivos

- Coleccion: `docs/postman/ordersmanagement.postman_collection.json`
- Environment local: `docs/postman/environments/ordersmanagement.local.postman_environment.json`
- Data file (Runner): `docs/postman/data/happy-path-and-errors.data.json`

## Que trae

- Flujo completo `happy path` para customers, products y orders.
- Dos errores controlados:
  - `INVALID_ORDER_STATUS` (HTTP 409)
  - `INSUFFICIENT_STOCK` (HTTP 400)
- Datos realistas para Colombia (nombres, telefonos, direcciones, productos, precios).

## Como ejecutarlo en Postman

1. Importa la coleccion y el environment local.
2. Selecciona el environment `ordersmanagement-local`.
3. Para ejecucion manual, usa la carpeta `01 - Happy Path` y luego `02 - Errores (2 casos)`.
4. Para ejecucion con runner, carga el archivo `happy-path-and-errors.data.json`.

## Notas

- La coleccion genera `customerIdentification` y `customerEmail` unicos con `suffix` para evitar colisiones.
- Si no usas runner data, toma valores por defecto realistas definidos en el script `prerequest`.
- Si reinicias la API, al usar H2 en memoria se limpia la data y puedes volver a ejecutar desde cero.
