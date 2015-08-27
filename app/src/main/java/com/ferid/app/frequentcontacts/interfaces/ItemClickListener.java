package com.ferid.app.frequentcontacts.interfaces;

/**
 * Created by ferid.cafer on 6/26/2015.
 */
public interface ItemClickListener {
    /**
     * On item click
     * @param contactPosition Position of selected contact
     * @param menuItemPosition Position of menu item
     */
    void OnItemClick(int contactPosition, int menuItemPosition);
}
