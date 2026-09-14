# Sistema de órdenes en línea — Chain of Responsibility

Implementación de la tarea *Análisis de problemas de diseño* (Diseño de Software Flexible y Reusable). El cliente crea órdenes en el sistema en línea pasa cada solicitud por una cadena de verificaciones independientes, antes de que la orden se procese de verdad.

## El problema

Un cliente que se conecta a un sistema de órdenes en línea debe pasar cada solicitud por varias verificaciones antes de crear la orden: autenticación, permisos, saneamiento de datos, control de intentos repetidos por IP, y una consulta de caché para no repetir trabajo.

## El patrón aplicado

**Chain of Responsibility.** Cada verificación es una clase independiente (`ordenes.chain.*Handler`) que hereda de `BaseHandler` y solo implementa su propia regla en `check(...)`. Ninguna verificación conoce a las demás; cada una solo sabe a cuál eslabón entregarle la solicitud si su propio chequeo pasa. `OrderClient` arma la cadena una sola vez y le entrega cada solicitud al primer eslabón.

```text
OrderClient -> RateLimitHandler -> AuthenticationHandler -> PermissionHandler
            -> SanitizationHandler -> CacheHandler -> (sistema real de órdenes)
```

`RateLimitHandler` va primero a propósito: si fuera después de `AuthenticationHandler`, nunca se alcanzaría mientras las credenciales sigan siendo incorrectas, porque la autenticación fallida ya detendría la cadena antes de llegar ahí.

## Una decisión del esquema: sin llave foránea entre `intentos_login` y `usuarios`

`intentos_login` guarda `nombre_usuario` como texto plano, no como llave foránea hacia `usuarios`, a propósito. `RateLimitHandler` necesita poder registrar intentos con un usuario que ni siquiera existe, que son justo los más relevantes para detectar fuerza bruta, y cuenta los intentos fallidos por `direccion_ip`, no por usuario. Exigir una llave foránea válida acoplaría esta tabla a la de autenticación, mezclando dos responsabilidades que cambian por razones distintas. Es la misma idea de bajo acoplamiento y responsabilidad única que ya guía la separación de los handlers en la cadena.

## Estructura del proyecto

```text
db/schema.sql                          Esquema MySQL (usuarios, intentos_login, respuestas_cacheadas)
src/main/java/ordenes/
  Main.java                            Demostración con los distintos escenarios
  chain/RequestHandler.java            Interfaz del patrón (rol Handler)
  chain/BaseHandler.java               Lógica común de la cadena (delegar o detener)
  chain/AuthenticationHandler.java     Verificación 1: credenciales
  chain/PermissionHandler.java         Verificación 2: permisos de administrador
  chain/SanitizationHandler.java       Verificación 3: saneo de datos
  chain/RateLimitHandler.java          Verificación 4: intentos fallidos por IP
  chain/CacheHandler.java              Verificación 5: respuesta cacheada
  chain/MaxSizeHandler.java            Verificación NUEVA, agregada sin tocar las anteriores
  chain/OrderClient.java               Rol Client: arma la cadena y la invoca
  model/OrderRequest.java              Datos que viajan por la cadena
  model/OrderResponse.java             Resultado de una verificación o de la orden real
  db/Database.java                     Conexión JDBC
  service/OrderService.java            Simulación del sistema real de órdenes
```

## Cómo ejecutarlo

1. Abrir `db/schema.sql` en MySQL Workbench y ejecutarlo contra el servidor local (crea la base `sistema_ordenes` y sus tablas).
2. Crear el usuario de la aplicación, si no existe:

   ```sql
   CREATE USER IF NOT EXISTS 'app_ordenes'@'localhost' IDENTIFIED BY 'AppOrdenes2026!';
   GRANT ALL PRIVILEGES ON sistema_ordenes.* TO 'app_ordenes'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Compilar y ejecutar:

   ```text
   mvn clean package
   java -jar target/order-system.jar
   ```

Si tu usuario o contraseña de MySQL son distintos, se pueden sobrescribir con las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD` sin tocar el código.

## Qué muestra la demostración (`Main`)

1. Una solicitud válida se procesa de punta a punta.
2. Credenciales incorrectas detienen la cadena en `AuthenticationHandler`.
3. Un usuario normal pidiendo una acción administrativa se detiene en `PermissionHandler`.
4. El mismo caso con un usuario administrador sí pasa.
5. Datos con contenido no permitido se detienen en `SanitizationHandler`.
6. La misma solicitud enviada dos veces: la segunda la resuelve `CacheHandler` sin volver a procesar la orden.
7. Varios intentos fallidos seguidos desde la misma IP activan el bloqueo de `RateLimitHandler`.
8. Una verificación nueva (`MaxSizeHandler`) se agrega al inicio de la cadena sin modificar ninguna de las clases existentes.
