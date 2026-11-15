#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EVID_DIR="$(cd "$PROJECT_DIR/../evidencias" && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
RUN_ID="$(date +%Y%m%d%H%M%S)"
mkdir -p "$EVID_DIR"

ready=0
for attempt in $(seq 1 40); do
  if curl -fsS "$BASE_URL/api/equipos" >/dev/null 2>&1; then ready=1; break; fi
  sleep 1
done
if [[ "$ready" -ne 1 ]]; then
  echo "La API no respondió en $BASE_URL tras 40 segundos. Ejecuta docker compose up -d --build y revisa sus logs." >&2
  exit 1
fi

request() {
  local name="$1" method="$2" path="$3" body="${4:-}" header_name="${5:-X-Client-Id}" header_value="${6-evidencia-g13}"
  local file="$EVID_DIR/${name}.txt"
  local header_argument="$header_name: $header_value"
  if [[ -z "$header_value" ]]; then header_argument="$header_name;"; fi
  {
    printf 'Solicitud: %s %s%s\n' "$method" "$BASE_URL" "$path"
    if [[ -n "$body" ]]; then printf 'Request headers: Content-Type: application/json; %s: %s\n' "$header_name" "$header_value"; printf 'Request body: %s\n' "$body"; else printf 'Request headers: %s: %s\n' "$header_name" "$header_value"; fi
    printf '\nRespuesta HTTP completa (status y headers incluidos):\n'
    if [[ -n "$body" ]]; then
      curl -sS -i -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -H "$header_argument" --data-raw "$body"
    else
      curl -sS -i -X "$method" "$BASE_URL$path" -H "$header_argument"
    fi
    printf '\n'
  } > "$file"
}
extract_id() { tail -n 1 "$EVID_DIR/$1.txt" | jq -er '.id'; }

request '01-get-equipos' GET /api/equipos
request '02-post-equipo' POST /api/equipos "{\"serial\":\"EQ-EVID-$RUN_ID\",\"nombre\":\"Equipo evidencia\",\"ubicacion\":\"Taller G13\"}"
EQUIPO_ID="$(extract_id 02-post-equipo)"
request '03-get-equipo' GET "/api/equipos/$EQUIPO_ID"
request '04-put-equipo' PUT "/api/equipos/$EQUIPO_ID" "{\"serial\":\"EQ-EVID-$RUN_ID\",\"nombre\":\"Equipo evidencia actualizado\",\"ubicacion\":\"Taller G13\",\"activo\":true}"
request '05-post-equipo-serial-duplicado' POST /api/equipos "{\"serial\":\"EQ-EVID-$RUN_ID\",\"nombre\":\"Duplicado\",\"ubicacion\":\"Taller\"}"

request '06-post-tecnico' POST /api/tecnicos "{\"nombres\":\"Técnico Evidencia $RUN_ID\",\"email\":\"tecnico-$RUN_ID@example.edu\",\"especialidad\":\"Mecánica\"}"
TECNICO_ID="$(extract_id 06-post-tecnico)"
request '07-get-tecnicos' GET /api/tecnicos
request '08-get-tecnico-especialidad-jpql' GET '/api/tecnicos?especialidad=Mec%C3%A1nica'
request '09-get-tecnico' GET "/api/tecnicos/$TECNICO_ID"
request '10-put-tecnico' PUT "/api/tecnicos/$TECNICO_ID" "{\"nombres\":\"Técnico Evidencia Actualizado $RUN_ID\",\"email\":\"tecnico-$RUN_ID@example.edu\",\"especialidad\":\"Mecánica\",\"activo\":true}"
request '11-post-tecnico-libre' POST /api/tecnicos "{\"nombres\":\"Técnico Libre $RUN_ID\",\"email\":\"libre-$RUN_ID@example.edu\",\"especialidad\":\"Eléctrica\"}"
TECNICO_LIBRE_ID="$(extract_id 11-post-tecnico-libre)"
request '12-delete-tecnico' DELETE "/api/tecnicos/$TECNICO_LIBRE_ID"

request '13-post-equipo-para-conflicto' POST /api/equipos "{\"serial\":\"EQ-CONFLICTO-$RUN_ID\",\"nombre\":\"Equipo conflicto\",\"ubicacion\":\"Taller G13\"}"
EQUIPO_CONFLICTO_ID="$(extract_id 13-post-equipo-para-conflicto)"
FECHA="$(date -d '+3 days 10:20' '+%Y-%m-%dT%H:%M:%S')"
request '14-post-mantenimiento' POST /api/mantenimientos "{\"equipoId\":$EQUIPO_ID,\"tecnicoId\":$TECNICO_ID,\"fechaProgramada\":\"$FECHA\",\"descripcion\":\"Mantenimiento evidencia $RUN_ID\"}"
MANTENIMIENTO_ID="$(extract_id 14-post-mantenimiento)"
request '15-get-mantenimientos' GET /api/mantenimientos
request '16-get-mantenimientos-pendientes' GET /api/mantenimientos/pendientes
request '17-get-mantenimiento-tecnico-jpql' GET "/api/mantenimientos?tecnicoId=$TECNICO_ID"
request '18-get-mantenimiento' GET "/api/mantenimientos/$MANTENIMIENTO_ID"
request '19-put-mantenimiento' PUT "/api/mantenimientos/$MANTENIMIENTO_ID" "{\"equipoId\":$EQUIPO_ID,\"tecnicoId\":$TECNICO_ID,\"fechaProgramada\":\"$FECHA\",\"descripcion\":\"Mantenimiento evidencia actualizado $RUN_ID\"}"
request '20-post-mantenimiento-hora-duplicada' POST /api/mantenimientos "{\"equipoId\":$EQUIPO_CONFLICTO_ID,\"tecnicoId\":$TECNICO_ID,\"fechaProgramada\":\"$FECHA\",\"descripcion\":\"Conflicto de horario\"}"
FECHA_PASADA="$(date -d '-1 day' '+%Y-%m-%dT10:00:00')"
request '21-post-mantenimiento-fecha-pasada' POST /api/mantenimientos "{\"equipoId\":$EQUIPO_CONFLICTO_ID,\"tecnicoId\":$TECNICO_ID,\"fechaProgramada\":\"$FECHA_PASADA\",\"descripcion\":\"Fecha pasada\"}"

request '22-post-orden' POST /api/ordenes "{\"mantenimientoId\":$MANTENIMIENTO_ID,\"descripcion\":\"Orden evidencia $RUN_ID\"}"
ORDEN_ID="$(extract_id 22-post-orden)"
request '23-get-ordenes' GET /api/ordenes
request '24-get-ordenes-estado' GET '/api/ordenes?estado=PENDIENTE'
request '25-get-ordenes-equipo-estado-jpql' GET "/api/ordenes/equipo/$EQUIPO_ID?estado=PENDIENTE"
request '26-get-orden' GET "/api/ordenes/$ORDEN_ID"
request '27-put-orden' PUT "/api/ordenes/$ORDEN_ID" "{\"mantenimientoId\":$MANTENIMIENTO_ID,\"descripcion\":\"Orden evidencia actualizada $RUN_ID\"}"
request '28-patch-orden-transicion-invalida' PATCH "/api/ordenes/$ORDEN_ID/estado" '{"estado":"COMPLETADA"}'
request '29-patch-orden-en-proceso' PATCH "/api/ordenes/$ORDEN_ID/estado" '{"estado":"EN_PROCESO"}'
request '30-patch-orden-completada' PATCH "/api/ordenes/$ORDEN_ID/estado" '{"estado":"COMPLETADA"}'
request '31-patch-orden-reapertura-invalida' PATCH "/api/ordenes/$ORDEN_ID/estado" '{"estado":"PENDIENTE"}'

# Fixtures separados demuestran eliminación exitosa sin alterar la evidencia persistente principal.
request '32-post-equipo-eliminable' POST /api/equipos "{\"serial\":\"EQ-DELETE-$RUN_ID\",\"nombre\":\"Temporal\",\"ubicacion\":\"Taller\"}"
EQUIPO_DELETE_ID="$(extract_id 32-post-equipo-eliminable)"
request '33-delete-equipo' DELETE "/api/equipos/$EQUIPO_DELETE_ID"
request '34-delete-equipo-con-mantenimiento' DELETE "/api/equipos/$EQUIPO_ID"
request '35-delete-tecnico-con-mantenimiento' DELETE "/api/tecnicos/$TECNICO_ID"
FECHA_AUX="$(date -d '+3 days 12:20' '+%Y-%m-%dT%H:%M:%S')"
request '36-post-mantenimiento-eliminable' POST /api/mantenimientos "{\"equipoId\":$EQUIPO_CONFLICTO_ID,\"tecnicoId\":$TECNICO_ID,\"fechaProgramada\":\"$FECHA_AUX\",\"descripcion\":\"Mantenimiento temporal\"}"
MANTENIMIENTO_AUX_ID="$(extract_id 36-post-mantenimiento-eliminable)"
request '37-post-orden-eliminable' POST /api/ordenes "{\"mantenimientoId\":$MANTENIMIENTO_AUX_ID,\"descripcion\":\"Orden temporal\"}"
ORDEN_AUX_ID="$(extract_id 37-post-orden-eliminable)"
request '38-delete-orden' DELETE "/api/ordenes/$ORDEN_AUX_ID"
request '39-delete-mantenimiento' DELETE "/api/mantenimientos/$MANTENIMIENTO_AUX_ID"
request '40-get-recurso-inexistente' GET /api/equipos/999999999
request '41-post-validacion-400' POST /api/equipos '{"serial":"","nombre":" ","ubicacion":""}'
request '42-interceptor-identificador-vacio-400' GET /api/equipos '' 'X-Client-Id' ''

cat > "$EVID_DIR/run-ids-$RUN_ID.txt" <<INFO
Ejecución: $RUN_ID
Equipo persistente: $EQUIPO_ID
Técnico persistente: $TECNICO_ID
Mantenimiento persistente: $MANTENIMIENTO_ID
Orden persistente: $ORDEN_ID
INFO
printf 'Evidencias HTTP guardadas en %s (42 archivos), más el resumen de IDs.\n' "$EVID_DIR"
printf 'Datos persistentes de esta ejecución: equipo=%s técnico=%s mantenimiento=%s orden=%s\n' "$EQUIPO_ID" "$TECNICO_ID" "$MANTENIMIENTO_ID" "$ORDEN_ID"
