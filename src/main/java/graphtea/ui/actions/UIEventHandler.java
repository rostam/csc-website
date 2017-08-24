// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/
package graphtea.ui.actions;


import graphtea.platform.core.AbstractAction;
import graphtea.platform.core.BlackBoard;
import graphtea.platform.core.Listener;
import graphtea.ui.UIUtils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Maps the events generated by menues and toolbars to their Matching Action
 *
 * @author azin azadi
 */
/*ie seri az eventa faghat kafie ke enable shan (mese add vertex).
// ie seri az eventa daran addListener mikonan ta kar konan (mese loadfile).
// mishe dar har soorat enablesh kard, badesh too blackboard gasht did
// chizi dare addListener mikone ia na. (ba action), age addListener mikard ie event
// barash tolid konim.
//---
//agar bekhaim oonaii ro ke mese load file hasan ie event barashoon befresim
// pas baiad ie standardi baraie esme logi ke tooie black board daran dashte bashim
// khob chon baiad befahmim ke aghajoon oon dare tooie che logi goosh mikone
// hala mishe esmesho az tooie xml khoond ia az rooie action fahmid. man be shakhse action ro
// tarjih midam
//..
// man oon standard ro UIEventData.name(action) entekhab mikonam
*/
public class UIEventHandler extends AbstractAction {
    public static final String ACTIONS_MAP = "blackboard action -> actions hashmap";
    public static final String CONF = "blackboard : Configuration of program";

    /**
     * constructor
     *
     * @param bb the blackboard of the action
     */
    public UIEventHandler(BlackBoard bb) {
        super(bb);
        listen4Event(UIUtils.getUIEventKey(""));
    }
    public void track(){}

    public void performAction(String eventName, Object value) {
        UIEventData data = blackboard.getData(UIEventData.name(""));
        String id = data.action;
        if (id != null) {
            enableAction(id);
            sendEventToAction(id, data);
        }
    }

    private void enableAction(String id) {
        AbstractAction action = getAction(id);
        if (action != null)
            action.enable();
        else {
            HashSet<Listener> listeners = blackboard.getListeners(UIUtils.getUIEventKey(id));
            if (listeners == null)
                System.err.println("Can't find action for id = " + id);
        }
    }

    /**
     * fetches the target action from blackboard
     *
     * @param id The id of the action
     */
    private AbstractAction getAction(String id) {
        //the action map is put in the blackboard in UIHandlerImpl class, at endActions() method.
        HashMap<String, AbstractAction> actionsmap = blackboard.getData(ACTIONS_MAP);
        return actionsmap.get(id);
    }

    @Override
    public boolean trackUndos() {
        return false;
    }

    /**
     * it first check that is there exists any actions that addListener for the event? (is there any log with the name
     * registered by UIEventData.name(id) in blackboard?)
     * and if the answer is true it sends the event to log.
     *
     * @param id The action id
     * @param uiEventData The corresponding event data
     */
    private void sendEventToAction(String id, UIEventData uiEventData) {
//        if (blackboard.contains(UIEventData.name(id)))
        blackboard.setData(UIEventData.name(id), uiEventData);
    }
}