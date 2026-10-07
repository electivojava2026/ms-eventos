# ms-eventos

Microservicio de **eventos**, **tipos de entrada** y **aforo** del caso EventPass (Spring Boot + PostgreSQL en Docker).
Los endpoints están protegidos con el **JWT que emite `ms-auth`** (mismo secreto) y según el rol: `USER` (comprador) o `STAFF`.

## Requisitos
- JDK 21
- Docker Desktop (con el motor corriendo)
- Git
- Para probar con roles en Postman: `ms-auth` corriendo en `http://localhost:8081` (de ahí salen los tokens)

## Cómo ejecutarlo

1. Clonar el repositorio
   ```bash
   git clone https://github.com/electivojava2026/ms-eventos.git
   cd ms-eventos
   ```
2. Levantar la base de datos (contenedor PostgreSQL, puerto 5434)
   ```bash
   docker compose up -d
   ```
3. Ejecutar los tests (usan H2 en memoria, no necesitan la BD)
   ```bash
   ./mvnw test            # Windows: .\mvnw.cmd test
   ```
4. Compilar y empaquetar
   ```bash
   ./mvnw clean package   # Windows: .\mvnw.cmd clean package
   ```
5. Ejecutar la aplicación (puerto 8082)
   ```bash
   java -jar target/ms-eventos-0.0.1-SNAPSHOT.jar
   ```

## Endpoints (base: `http://localhost:8082/api/v1/eventos`)

Todos requieren el header `Authorization: Bearer <token>`.
Sin token → **401**. Con token pero sin el rol necesario → **403**.

| Método | Ruta | Descripción | Rol | Respuesta |
|--------|------|-------------|-----|-----------|
| GET | `/` | Listar eventos y su aforo disponible | USER, STAFF | 200 |
| GET | `/{id}` | Obtener un evento | USER, STAFF | 200 / 404 |
| POST | `/` | Crear evento (con tipos de entrada) | STAFF | 201 / 400 |
| PUT | `/{id}` | Actualizar datos del evento | STAFF | 200 / 400 / 404 |
| DELETE | `/{id}` | Eliminar evento | STAFF | 204 / 404 |
| POST | `/{eventoId}/tipos-entrada` | Agregar un tipo de entrada | STAFF | 201 / 400 / 404 |
| POST | `/{eventoId}/tipos-entrada/{tipoId}/reservas` | Verificar y descontar aforo (lo usa la emisión de tickets) | STAFF | 200 / 400 / 404 / **409 sin aforo** |

El descuento de aforo es **atómico** (un solo `UPDATE ... WHERE aforoDisponible >= cantidad`), por lo que
compras simultáneas no pueden superar la capacidad.

Ejemplo de body para `POST` / `PUT`:
```json
{
  "nombre": "Concierto Rock",
  "descripcion": "Show en vivo",
  "lugar": "Estadio Nacional",
  "fechaEvento": "2026-12-15T20:00:00",
  "tiposEntrada": [
    { "nombre": "General", "precio": 25000, "aforo": 500 },
    { "nombre": "VIP", "precio": 60000, "aforo": 100 }
  ]
}
```
Body para reservar aforo: `{ "cantidad": 3 }`.

Colección de Postman lista para importar: `postman/ms-eventos.postman_collection.json`
(hace login en ms-auth y guarda los tokens `STAFF` y `USER` automáticamente).

## Base de datos
- Imagen: `postgres:16` (ver `compose.yaml`) · BD `eventos_db` · usuario/clave `eventpass`
- Ver los datos desde el contenedor:
  ```bash
  docker exec -it eventpass-eventos-db psql -U eventpass -d eventos_db -c "select * from tipos_entrada;"
  ```
- Apagar: `docker compose down` (agregar `-v` para borrar los datos)

## Estructura
```
controller/  -> endpoints REST
service/     -> lógica de negocio, validaciones y reserva de aforo
repository/  -> acceso a datos (Spring Data JPA)
model/       -> entidades JPA (Evento 1---N TipoEntrada)
dto/         -> objetos de entrada/salida de la API
exception/   -> manejo de errores (400 / 404 / 409)
security/    -> validación del JWT de ms-auth y reglas por rol
```
