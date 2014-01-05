/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tal.org.eq.demo;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

import javax.microedition.amms.*;   
import javax.microedition.amms.control.*;   
import javax.microedition.amms.control.audioeffect.*;

/**
 *
 * @author milang
 */
public class EqualizerDemoMidlet extends MIDlet implements         
        CommandListener,
        PlayerListener {
    
    private Form form;
    private StringItem playButton;
    
    private Player player = null;
    private VolumeControl control = null;
    private EqualizerControl equalizerControl = null;    
    
    public EqualizerDemoMidlet() {
        form=new Form("EqDemo");
        
        playButton=new StringItem("Play", null, StringItem.BUTTON);
        form.append(playButton);
        
        equalizerControl=(EqualizerControl)GlobalManager.getControl("javax.microedition.amms.control.audioeffect.EqualizerControl");
        if (equalizerControl!=null) {
            equalizerControl.setEnabled(true);            
        } else {
            showError("Equalizer is not supported on your device. Application will now exit.");
            exitMIDlet();
        }
        
        player.addPlayerListener(this);
    }
    
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        Display display = getDisplay();        
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }
    
    public final Display getDisplay() {
        return Display.getDisplay(this);
    }

    public final Alert getErrorAlert(String msg) {
        Alert erro = new Alert("Error", msg, null, AlertType.ERROR);
        erro.setTimeout(Alert.FOREVER);
        return erro;
    } 
    
    public final void showError(String msg) {        
        getDisplay().setCurrent(getErrorAlert(msg));
    }
    
    public final void exitMIDlet() {        
        switchDisplayable(null, null);
        notifyDestroyed();
    }
    
    public final void startApp() {
        switchDisplayable(null, form);
    }
    
    public final void pauseApp() {
    }
    
    public final void destroyApp(boolean unconditional) {
    }

    public final void commandAction(Command c, Displayable d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
