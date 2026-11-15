# Programación bloqueante y no bloqueante

Una operación bloqueante detiene el hilo que la ejecuta hasta que termina. Por
ejemplo, enviar una notificación y esperar dos segundos con `Thread.sleep` deja
ocupado el hilo de la petición. Si llegan muchas solicitudes, habrá menos hilos
disponibles para atenderlas.

Como alternativa, el método `notificarAsignacion` usa `@Async` y devuelve un
`CompletableFuture`. La solicitud puede continuar mientras un hilo del grupo de
notificaciones realiza el envío en segundo plano.

La ventaja es que la API responde mejor y puede atender más solicitudes
concurrentes. La limitación es que el resultado no está disponible de inmediato:
hay que observar el `CompletableFuture` y manejar errores. También se debe
configurar un grupo de hilos con límites para no consumir recursos sin control.
