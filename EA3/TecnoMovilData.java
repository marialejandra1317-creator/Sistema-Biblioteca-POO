import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 1. Modelo Inmutable: Usamos 'final' y omitimos los metodos 'set'
final class RegistroViaje {
    private final String idUsuario;
    private final String ruta;
    private final String estacion;
    private final String accion; // "ENTRADA" o "SALIDA"
    private final LocalDateTime timestamp;

    public RegistroViaje(String idUsuario, String ruta, String estacion, String accion, LocalDateTime timestamp) {
        this.idUsuario = idUsuario;
        this.ruta = ruta;
        this.estacion = estacion;
        this.accion = accion;
        this.timestamp = timestamp;
    }

    // Getters para leer los datos (Los 5 indispensables)
    public String getIdUsuario() { return idUsuario; }
    public String getRuta() { return ruta; }
    public String getEstacion() { return estacion; }
    public String getAccion() { return accion; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

public class TecnoMovilData {
    public static void main(String[] args) {
        // 2. Lista Inmutable simulada usando List.of()
        List<RegistroViaje> viajes = List.of(
            new RegistroViaje("U01", "Ruta1", "Estacion Norte", "ENTRADA", LocalDateTime.of(2026, 9, 1, 7, 15)),
            new RegistroViaje("U02", "Ruta1", "Estacion Norte", "ENTRADA", LocalDateTime.of(2026, 9, 1, 7, 30)),
            new RegistroViaje("U01", "Ruta1", "Estacion Sur", "SALIDA", LocalDateTime.of(2026, 9, 1, 8, 0)),
            new RegistroViaje("U03", "Ruta2", "Estacion Centro", "ENTRADA", LocalDateTime.of(2026, 9, 1, 18, 45))
        );

        // Tarea A: Calcular afluencia por estación (Filtramos solo ENTRADAS y agrupamos)
        Map<String, Long> afluencia = viajes.stream()
            .filter(v -> v.getAccion().equals("ENTRADA"))
            .collect(Collectors.groupingBy(RegistroViaje::getEstacion, Collectors.counting()));
        System.out.println("Afluencia por estacion (Entradas): " + afluencia);

        // Tarea B: Identificacion de horas pico (Agrupamos por la hora del dia y contamos)
        Map<Integer, Long> flujoPorHora = viajes.stream()
            .collect(Collectors.groupingBy(v -> v.getTimestamp().getHour(), Collectors.counting()));
        System.out.println("Flujo de pasajeros por hora del dia: " + flujoPorHora);

        // Tarea C: Analisis de rutas mas utilizadas
        Map<String, Long> rutasMasUtilizadas = viajes.stream()
            .collect(Collectors.groupingBy(RegistroViaje::getRuta, Collectors.counting()));
        System.out.println("Rutas mas utilizadas: " + rutasMasUtilizadas);

        // Tarea D: Patrones de viaje por usuario
        Map<String, List<String>> patronesPorUsuario = viajes.stream()
            .collect(Collectors.groupingBy(
                RegistroViaje::getIdUsuario,
                Collectors.mapping(RegistroViaje::getEstacion, Collectors.toList())
            ));
        System.out.println("Patrones de viaje por usuario: " + patronesPorUsuario);
    
// TAREA E: Cálculo del tiempo promedio entre estaciones (Entrada a Salida)
double tiempoPromedioMinutos =viajes.stream()
   // 1. Agrupamos los registros por cada idUsuario
   .collect(Collectors.groupingBy(RegistroViaje::getIdUsuario))
   .values().stream()
   // 2. Filtramo solo los usuarios que tienen al menos 2 registros (entrada y salida)
   .filter(registrosUsuario -> registrosUsuario.size() >= 2)
   .mapToLong(registrosUsuario -> {
       // Buscamos el timestamp de ENTRADA
       LocalDateTime entrada = registrosUsuario.stream()
       .filter(r -> r.getAccion().equalsIgnoreCase("ENTRADA"))
       .map(RegistroViaje::getTimestamp)
       .findFirst()
       .orElse(null);

       // Buscamos el timestamp de SALIDA
       LocalDateTime salida = registrosUsuario.stream()
       .filter(r -> r.getAccion().equalsIgnoreCase("SALIDA"))
       .map(RegistroViaje::getTimestamp)
       .findFirst()
       .orElse(null);

       // Calculamos la diferencia en minutos si ambos registros existen
       if (entrada !=null && salida != null) {
           return Duration.between(entrada, salida).toMinutes();
       }
       return 0L;
   })
   .filter(duracion -> duracion > 0)
   .average()
   .orElse(0.0);

   System.out.println("Tarea E - Tiempo promedio entre estaciones: " + tiempoPromedioMinutos + "minutos");

   // TAREA F: Detección de sobrecarga en rutas usando parallelStream()
   long umbralSobrecarga = 3L; // Limite de viajes configurado para alertas

   Map<String, String> estadoRutas = viajes.parallelStream() // Procesamiento paralelo sin efectos secundarios
   .collect(Collectors.groupingBy(RegistroViaje::getRuta, Collectors.counting()))
   .entrySet().stream()
   .collect(Collectors.toMap(
    Map.Entry::getKey,
    entry -> entry.getValue() >= umbralSobrecarga ? "CRÍTICA" : "NORMAL"
   ));

   System.out.println("Tarea F - Detección de sobrecarga en rutas: " + estadoRutas);
   }
}





