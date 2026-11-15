# Middleware y programación orientada a aspectos

## Middleware HTTP

`HttpLoggingInterceptor` se ejecuta alrededor de las rutas `/api/**`. Antes de
la ejecución valida `X-Client-Id` cuando la cabecera está presente. Una cabecera
vacía produce una respuesta `400`. Al terminar registra método, ruta, IP,
estado y duración, y agrega `X-Response-Time`.

## AOP

`ServiceAuditAspect` aplica un consejo `@Around` a las clases del paquete
`ec.edu.espe.mantenimiento.service`. Así se mide cada llamada del servicio, se
registra su finalización y se registra el error antes de propagarlo. Esto evita
duplicar código de auditoría en cada método.
