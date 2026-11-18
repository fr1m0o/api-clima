package aplicacion.clima;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.json.simple.JSONObject;

/**
 * Clase principal que representa la ventana de la aplicación de clima.
 * Permite al usuario ingresar una ubicación, consultar datos climáticos
 * y visualizar la información actual del clima con imágenes y texto.
 * 
 * Hereda de JFrame y usa Swing para la interfaz gráfica.
 * 
 * @author nicolas
 */
public class APLICACIONCLIMA extends JFrame {

    private JSONObject climaDato;          // Datos del clima obtenidos de la API
    private JTextField txtTexto;           // Campo para ingresar la ubicación
    private JLabel condicionclimaImage;   // Imagen que muestra la condición del clima
    private JLabel txtTemperatura;         // Etiqueta que muestra la temperatura actual
    private JLabel condicionclimaDesc;    // Etiqueta que muestra la descripción textual del clima
    private JLabel humedadtxt;             // Etiqueta que muestra la humedad relativa
    private JLabel vientotxt;              // Etiqueta que muestra la velocidad del viento

    /**
     * Constructor que configura la ventana principal de la aplicación.
     */
    public APLICACIONCLIMA() {
        super("Clima");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null); // Centra la ventana en pantalla
        setLayout(null);
        setResizable(false);
        addGuiComponents();
    }

    /**
     * Método que agrega todos los componentes gráficos a la ventana.
     */
    private void addGuiComponents() {
        agregarBotonBusqueda();
        agregarImagenClima();
        TextoClima();
    }

    /**
     * Agrega el campo de texto para ingresar la ubicación y el botón de búsqueda.
     * El botón realiza la consulta del clima y actualiza la interfaz según los datos obtenidos.
     */
    private void agregarBotonBusqueda() {
        txtTexto = new JTextField();
        txtTexto.setBounds(15, 15, 351, 45);
        txtTexto.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(txtTexto);

        JButton searchButton = new JButton(loadImage("src/images/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);

        // Acción al hacer click en el botón de búsqueda
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = txtTexto.getText();
                // Validar que el texto no esté vacío ni solo espacios
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                // Obtener datos del clima para la ubicación ingresada
                climaDato = Appclima.getClimaDatos(userInput);
                if (climaDato == null) {
                    JOptionPane.showMessageDialog(null, "No se encontraron datos.");
                    return;
                }

                // Obtener la condición climática y actualizar la imagen correspondiente
                String condicionclima = ((String) climaDato.get("weather_condition")).toLowerCase();

                switch (condicionclima) {
                    case "clear":
                        condicionclimaImage.setIcon(loadImage("src/images/clear.png"));
                        break;
                    case "cloudy":
                        condicionclimaImage.setIcon(loadImage("src/images/cloudy.png"));
                        break;
                    case "rain":
                        condicionclimaImage.setIcon(loadImage("src/images/rain.png"));
                        break;
                    case "snow":
                        condicionclimaImage.setIcon(loadImage("src/images/snow.png"));
                        break;
                    default:
                        condicionclimaImage.setIcon(loadImage("src/images/cloudy.png"));
                        break;
                }

                // Actualizar temperatura
                Double temperatura = (Double) climaDato.get("temperatura");
                txtTemperatura.setText(temperatura + "°C");

                // Actualizar descripción del clima
                condicionclimaDesc.setText(condicionclima);

                // Actualizar humedad relativa
                Long humedad = (Long) climaDato.get("humidity");
                humedadtxt.setText("<html><b>Humidity:</b> " + humedad + "%</html>");

                // Actualizar velocidad del viento
                Double viento = (Double) climaDato.get("wind_speed");
                vientotxt.setText("<html><b>Wind:</b> " + viento + " KM/H</html>");
            }
        });

        add(searchButton);
    }

    /**
     * Agrega las imágenes fijas de la condición del clima, humedad y viento.
     */
    private void agregarImagenClima() {
        condicionclimaImage = new JLabel(loadImage("src/images/cloudy.png"));
        condicionclimaImage.setBounds(0, 125, 450, 217);
        add(condicionclimaImage);

        JLabel humedadImage = new JLabel(loadImage("src/images/humidity.png"));
        humedadImage.setBounds(15, 500, 74, 66);
        add(humedadImage);

        JLabel vientoImage = new JLabel(loadImage("src/images/windspeed.png"));
        vientoImage.setBounds(220, 500, 74, 66);
        add(vientoImage);
    }

    /**
     * Agrega etiquetas para mostrar el texto del clima:
     * temperatura, descripción, humedad y velocidad del viento.
     */
    private void TextoClima() {
        txtTemperatura = new JLabel("10°C");
        txtTemperatura.setBounds(0, 350, 450, 54);
        txtTemperatura.setFont(new Font("Dialog", Font.BOLD, 48));
        txtTemperatura.setHorizontalAlignment(SwingConstants.CENTER);
        add(txtTemperatura);

        condicionclimaDesc = new JLabel("nublado");
        condicionclimaDesc.setBounds(0, 405, 450, 36);
        condicionclimaDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        condicionclimaDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(condicionclimaDesc);

        humedadtxt = new JLabel("<html><b>Humidity:</b> 100%</html>");
        humedadtxt.setBounds(90, 500, 85, 55);
        humedadtxt.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humedadtxt);

        vientotxt = new JLabel("<html><b>Wind:</b> 10 KM/H</html>");
        vientotxt.setBounds(310, 500, 85, 55);
        vientotxt.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(vientotxt);
    }

    /**
     * Carga una imagen desde el sistema de archivos y la escala a 64x64 píxeles.
     * 
     * @param resourcePath Ruta del archivo de imagen
     * @return ImageIcon escalado o null si hubo error
     */
    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Error cargando imagen: " + resourcePath);
            e.printStackTrace();
        }
        return null;
    }
}
