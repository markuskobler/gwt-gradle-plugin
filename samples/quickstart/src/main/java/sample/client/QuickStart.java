package sample.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *
 * @author Markus Kobler
 */
public class QuickStart implements EntryPoint {


    public void go(Panel panel) {
        Button button = new Button("Click me");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert("Hello World");
            }
        });

        panel.add(button);
    }




    public void onModuleLoad() {
        go(RootPanel.get());
    }
    
}
