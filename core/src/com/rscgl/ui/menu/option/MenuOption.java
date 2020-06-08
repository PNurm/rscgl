package com.rscgl.ui.menu.option;

import com.badlogic.gdx.graphics.Color;
import com.rscgl.ui.menu.GameAction;

public class MenuOption {
    private String action;
    private String subject;
    private Color subjectColor;

    public void setOptionAction(GameAction optionAction) {
        this.optionAction = optionAction;
    }

    private GameAction optionAction;

    public void setPriority(OptionPriority priority) {
        this.priority = priority;
    }

    private OptionPriority priority;

    public MenuOption(String action, String subject, Color subjectColor, GameAction optionAction, OptionPriority priority) {
        this.action = action;
        this.subject = subject;
        this.subjectColor = subjectColor;
        this.optionAction = optionAction;
        this.priority = priority;
    }

    public MenuOption() {}

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Color getSubjectColor() {
        return subjectColor;
    }

    public void setSubjectColor(Color subjectColor) {
        this.subjectColor = subjectColor;
    }

    public GameAction getOptionAction() {
        return optionAction;
    }

    public OptionPriority getPriority() {
        return priority;
    }
}
