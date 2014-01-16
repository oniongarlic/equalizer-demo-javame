/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tal.org.eq.demo;

import java.io.*;

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
        ItemStateListener,
        ItemCommandListener,
        PlayerListener {
    
    private Form form;
    private Form btequalizerForm;    
    private Form equalizerForm;
    private Form presetForm;
        
    private StringItem playButton;
    
    private Command backCommand;
    private Command exitCommand;
    
    private Command bassTrebleCommand;
    private Command equalizerCommand;
    private Command presetCommand;    
    private Command playCommand;
    
    private Player player = null;    
    private EqualizerControl equalizerControl = null;    
    
    // Bass and Treble controls
    private Gauge bassGauge;
    private Gauge trebleGauge;
    
    // Equalizer bands and controls
    private int bands;
    private int bandMinLevel;
    private int bandMaxLevel;
    private Gauge [] eqGauges;
    private int steps=15; // Adjust for more eq steps
    
    // Equalizer presets
    private String [] presets;    
    private ChoiceGroup presetChoice;

    public EqualizerDemoMidlet() {
        
        equalizerControl=(EqualizerControl)GlobalManager.getControl("javax.microedition.amms.control.audioeffect.EqualizerControl");
        if (equalizerControl!=null) {
            equalizerControl.setEnabled(true);            
        } else {
            showError("Equalizer is not supported on your device. Application will now exit.");
            exitMIDlet();
        }
        
        bands = equalizerControl.getNumberOfBands();
        bandMinLevel = equalizerControl.getMinBandLevel();
        bandMaxLevel = equalizerControl.getMaxBandLevel();
        
        Log.log("EqBands: "+bands);       
        Log.log("EqBandsMinLevel: "+bandMinLevel);
        Log.log("EqBandsMaxLevel: "+bandMaxLevel);
        
        presets = equalizerControl.getPresetNames();        
        Log.log("Presets available:");
        if (presets!=null)
            Log.printArray(presets);        
        
        form=new Form("AMMS EqDemo");
        
        playButton=new StringItem("Example music", "Start Play", StringItem.BUTTON);
        playCommand = new Command("Play", Command.ITEM, 1);
        playButton.setDefaultCommand(playCommand);
        playButton.setItemCommandListener(this);
        form.append(playButton);
        
        backCommand = new Command("Back", Command.BACK, 1);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        bassTrebleCommand = new Command("Bass/Treble", Command.SCREEN, 1);
        equalizerCommand = new Command("Equalizer", Command.SCREEN, 1);
        presetCommand = new Command("Preset", Command.SCREEN, 1);
        
        form.addCommand(exitCommand);
        form.addCommand(bassTrebleCommand);
        form.addCommand(equalizerCommand);
        form.addCommand(presetCommand);
        
        try {
            InputStream audioStream=getClass().getResourceAsStream("/try_again.mp3");
            if (audioStream==null)
                Log.log("Failed to get audio resource ?");
            player = Manager.createPlayer(audioStream, "audio/mp3");
            player.setLoopCount(-1);
            player.realize();
        } catch (Exception e) {
            Log.loge("PLayerE", e);
            showError("Failed to create meida player.");
            exitMIDlet();
        }
        
        form.setCommandListener(this);        
        player.addPlayerListener(this);
    }
    
    public Form getFormEqualizerBassTreble() {
        if (btequalizerForm == null) {
            btequalizerForm = new Form("Bass/Treble");            
                        
            bassGauge = new Gauge("Bass", true, 10, 5);
            bassGauge.setLayout(Item.LAYOUT_EXPAND);            

            trebleGauge = new Gauge("Treble", true, 10, 5);
            trebleGauge.setLayout(Item.LAYOUT_EXPAND);            
                        
            btequalizerForm.append(bassGauge);
            btequalizerForm.append(trebleGauge);            
            
            btequalizerForm.addCommand(backCommand);
            btequalizerForm.setCommandListener(this);
            btequalizerForm.setItemStateListener(this);
        }
        return btequalizerForm;
    }
    
    // Adjust the band min/max/values to 0-max so we can use them with Gauge controls
    private int scaleBandValue(int minL, int maxL, int level, int maxV) {        
        return (level-minL)*(maxV/(maxL-minL));
    }
    
    private int getBandValue(int minL, int maxL, int level, int maxV) {
        return minL + level * (maxL - minL) / maxV;
    }
    
    public Form getFormEqualizer() {
        if (equalizerForm == null) {
            int level;

            equalizerForm = new Form("Equalizer");          
            eqGauges = new Gauge[bands];

            for (int i=0;i<bands;i++) {
                level = equalizerControl.getBandLevel(i);
                int blevel=scaleBandValue(bandMinLevel, bandMaxLevel, level, steps);
                
                eqGauges[i] = new Gauge("Band "+equalizerControl.getCenterFreq(i), true, steps, blevel);
                eqGauges[i].setLayout(Item.LAYOUT_EXPAND);
                equalizerForm.append(eqGauges[i]);
            }                                                
            
            equalizerForm.addCommand(backCommand);
            equalizerForm.setCommandListener(this);
            equalizerForm.setItemStateListener(this);
        }
        return equalizerForm;
    }
    
    public Form getFormPreset() {
        if (presetForm == null) {
            presetForm = new Form("Presets");
            
            presetChoice = new ChoiceGroup("Presets", Choice.EXCLUSIVE);            
            if (presets != null && presets.length > 0) {
                for (int i = 0; i < presets.length; ++i)
                    presetChoice.append(presets[i], null);
            }
            presetForm.append(presetChoice);
            
            presetForm.addCommand(backCommand);
            presetForm.setCommandListener(this);
            presetForm.setItemStateListener(this);
        }
        return presetForm;
    }
    
    public void itemStateChanged(Item item) {        
        if (item == bassGauge) {
            Log.log("SetBass");
            equalizerControl.setBass(bassGauge.getValue()*10);
        } else if (item == trebleGauge) {
            Log.log("SetTreble");
            equalizerControl.setTreble(trebleGauge.getValue()*10);
        } else if (item == presetChoice) {
            Log.log("SetPreset");
            equalizerControl.setPreset(presetChoice.getString(presetChoice.getSelectedIndex()));        
        } else {
            for (int i=0;i<bands;i++) {
                if (item == eqGauges[i]) {
                    int v=eqGauges[i].getValue();
                    equalizerControl.setBandLevel(getBandValue(bandMinLevel, bandMaxLevel, v, steps), i);
                    return;
                }
            }
            Log.log("[UnknownItemStateChange]");
        }
    }
    
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == form) {
            if (command == exitCommand) {
                exitMIDlet();
            } else if (command == bassTrebleCommand) {
                switchDisplayable(null, getFormEqualizerBassTreble());            
            } else if (command == equalizerCommand) {
                switchDisplayable(null, getFormEqualizer());            
            } else if (command == presetCommand) {
                switchDisplayable(null, getFormPreset());            
            }
        } else if (displayable == equalizerForm) {
            if (command == backCommand) {                
                switchDisplayable(null, form);
            }
        } else if (displayable == presetForm) {
            if (command == backCommand) {                
                switchDisplayable(null, form);
            }
        } else if (displayable == btequalizerForm) {
            if (command == backCommand) {                
                switchDisplayable(null, form);
            }
        }
    }
    
    public void commandAction(Command c, Item item) {
        Log.log("CA: " + c + " Item: " + item);
        if (c == playCommand) {
            playStart();
        }
        Log.log("Unknown CA?");
    }
    
    public void playStart() {
         try {
             VolumeControl control;
             control = (VolumeControl)player.getControl("VolumeControl");
             control.setLevel(100);
             control.setMute(false);
             player.start();
        } catch (MediaException e) {
            Log.loge("PlayerE:", e);
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        Log.log("PlayerUpdate: "+event);
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
        player.close();
    }
}
