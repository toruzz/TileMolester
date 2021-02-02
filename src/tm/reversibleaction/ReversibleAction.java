/*
*
*    Copyright (C) 2003 Kent Hansen.
*
*    This file is part of Tile Molester.
*
*    Tile Molester is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    Tile Molester is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*/

package tm.reversibleaction;

import java.util.Date;

/**
*
* Abstract class for handling reversible actions (undoable/redoable).
* Every type of action needs to subclass this class and implement the
* below methods.
*
**/

public abstract class ReversibleAction {

    private String presentationName;
    private Date timeStamp;

/**
*
* Creates a reversible action with the given presentation name.
*
**/

    public ReversibleAction(String presentationName) {
        this.presentationName = presentationName;
        timeStamp = new Date();
    }

/**
*
* Subclasses must implement these.
*
**/

    public abstract void undo();
    public abstract void redo();
    public abstract boolean canUndo();
    public abstract boolean canRedo();

/**
*
* Gets the presentation name.
* The presentation name is the text that is displayed after "Undo"/"Redo"
* when this action can be undone/redone.
*
**/

    public String getPresentationName() {
        return presentationName;
    }

/**
*
* Gets the timestamp for this action, i.e. when it was first executed.
*
**/

    public Date getTimeStamp() {
        return timeStamp;
    }

}