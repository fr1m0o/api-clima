package aplicacion.clima;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Clase que proporciona métodos para obtener datos climáticos a partir
 * de una ubicación usando APIs públicas de Open-Meteo.
 * 
 * @author nicolas
 */
public class Appclima {

    /**
     * Obtiene los datos climáticos actuales (temperatura, condición,
     * humedad y velocidad del viento) para una ubicación dada.
     * 
     * @param Nombrelocalizacion Nombre de la ubicación a consultar.
     * @return JSONObject con los datos climáticos, o null si hubo error.
     */
    public static JSONObject getClimaDatos(String Nombrelocalizacion) {
        // Obtener datos de ubicación (latitud, longitud)
        JSONArray locationData = getlocationData(Nombrelocalizacion);
        if (locationData == null || locationData.isEmpty()) {
            System.out.println("No se encontró la ubicación.");
            return null;
        }

        // Por ahora se usan coordenadas fijas (Buenos Aires)
        // Se podría extraer lat/lon de locationData para más precisión
        JSONObject local = (JSONObject) locationData.get(0);
        double latitud = -34.6037;
        double longitud = -58.3816;

        // Construir URL para API Open-Meteo con parámetros de consulta
        String urlString = "https://api.open-meteo.com/v1/forecast?"
                + "latitude=" + latitud + "&longitude=" + longitud
                + "&hourly=temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m&timezone=auto";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Validar conexión y código HTTP 200 OK
            if (conn == null || conn.getResponseCode() != 200) {
                System.out.println("Error en la conexión con la API del clima.");
                return null;
            }

            // Leer respuesta JSON línea por línea
            StringBuilder resultJSon = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJSon.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parsear la respuesta JSON
            JSONParser parser = new JSONParser();
            JSONObject resultJSONObject = (JSONObject) parser.parse(resultJSon.toString());

            // Extraer la sección "hourly" con los datos horarios
            JSONObject horario = (JSONObject) resultJSONObject.get("hourly");

            // Obtener arreglo de tiempos horarios para buscar el índice de la hora actual
            JSONArray time = (JSONArray) horario.get("time");
            int index = findIndexOfCurrentTime(time);

            // Si no se encuentra la hora actual, retornar null
            if (index == -1) {
                System.out.println("Hora actual no encontrada en el JSON.");
                return null;
            }

            // Extraer datos específicos de clima usando el índice encontrado
            JSONArray datoTemperatura = (JSONArray) horario.get("temperature_2m");
            Double temperatura = ((Number) datoTemperatura.get(index)).doubleValue();

            JSONArray codigoclima = (JSONArray) horario.get("weathercode");
            String weatherCondition = converWeatherCode(((Number) codigoclima.get(index)).longValue());

            JSONArray humedadRelativa = (JSONArray) horario.get("relative_humidity_2m");
            Long humedad = ((Number) humedadRelativa.get(index)).longValue();

            JSONArray velocidadVientoDatos = (JSONArray) horario.get("wind_speed_10m");
            Double VelocidadViento = ((Number) velocidadVientoDatos.get(index)).doubleValue();

            // Construir un JSONObject con los datos de clima para retornar
            JSONObject climaDatos = new JSONObject();
            climaDatos.put("temperatura", temperatura);
            climaDatos.put("weather_condition", weatherCondition);
            climaDatos.put("humidity", humedad);
            climaDatos.put("wind_speed", VelocidadViento);

            return climaDatos;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene datos de ubicación (latitud, longitud, etc.) a partir del
     * nombre de la localidad consultada mediante la API de geocoding.
     * 
     * @param Nombrelocalizacion Nombre de la localidad a buscar.
     * @return JSONArray con resultados de ubicación, o null si error.
     */
    public static JSONArray getlocationData(String Nombrelocalizacion) {
        // Construcción de la URL para la API de geocoding
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name="
                + Nombrelocalizacion + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn == null || conn.getResponseCode() != 200) {
                System.out.println("Error en la conexión con la API de geocoding.");
                return null;
            } else {
                // Leer la respuesta JSON
                StringBuilder resultJSon = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNext()) {
                    resultJSon.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect();

                // Parsear JSON y obtener arreglo "results"
                JSONParser parser = new JSONParser();
                JSONObject resultJSONObject = (JSONObject) parser.parse(resultJSon.toString());
                JSONArray locationData = (JSONArray) resultJSONObject.get("results");

                return locationData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Realiza la conexión HTTP GET a una URL y retorna el HttpURLConnection
     * para poder leer la respuesta.
     * 
     * @param urlString URL a conectar.
     * @return HttpURLConnection si la conexión fue exitosa, null en caso contrario.
     */
    public static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca en el arreglo de tiempos horarios el índice que coincide con
     * la hora actual formateada.
     * 
     * @param timeList JSONArray con strings de tiempos en formato ISO.
     * @return índice del tiempo que coincide con la hora actual, o -1 si no se encuentra.
     */
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }

        // Retornar -1 si no se encuentra la hora actual
        return -1;
    }

    /**
     * Obtiene la fecha y hora actual formateada en el patrón
     * "yyyy-MM-dd'T'HH:00" que usa la API para el tiempo horario.
     * 
     * @return String con fecha y hora actual formateada.
     */
    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    /**
     * Convierte un código numérico de condición climática (weathercode)
     * en una descripción de texto simplificada.
     * 
     * @param codigoclima Código numérico del clima según Open-Meteo.
     * @return String con la condición del clima ("clear", "cloudy", "rain", "snow").
     */
    private static String converWeatherCode(long codigoclima) {
        String weatherCondition = "";
        if (codigoclima == 0L) {
            weatherCondition = "clear";
        } else if (codigoclima <= 3L && codigoclima > 0L) {
            weatherCondition = "cloudy";
        } else if ((codigoclima >= 51L && codigoclima <= 67L)
                || (codigoclima >= 80L && codigoclima <= 99L)) {
            weatherCondition = "rain";
        } else if (codigoclima >= 71L && codigoclima <= 77L) {
            weatherCondition = "snow";
        }
        return weatherCondition;
    }
}
