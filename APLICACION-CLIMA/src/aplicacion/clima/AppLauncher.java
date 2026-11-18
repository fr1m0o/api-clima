/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aplicacion.clima;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author nicolas
 */
public class AppLauncher {
    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
            
               new APLICACIONCLIMA().setVisible(true);//
                System.out.println(Appclima.getlocationData("Argentina"));
                System.out.println(Appclima.getCurrentTime());
            }
        
        
        
        
        
        
        });
    }
}
