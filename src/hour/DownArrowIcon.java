/*
 * File: DownArrowIcon.java in java package hour is part of application
 * PlanetHour v1.0 - Planetary hour calculation software
 * Copyright (C) 2014 Sounak Choudhury
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact E-mail: sounak_s@rediffmail.com
 */
package hour;

/**
 *
 * @author Sounak Choudhury
 */
import java.awt.*;
import javax.swing.*;

public class DownArrowIcon implements Icon {

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        JComponent component = (JComponent) c;
        int iconWidth = getIconWidth();
        g.translate(x, y);
        g.setColor(component.isEnabled() ? Color.black : Color.gray);
        g.drawLine(0, 0, iconWidth - 1, 0);
        g.drawLine(1, 1, 1 + (iconWidth - 3), 1);
        g.drawLine(2, 2, 2 + (iconWidth - 5), 2);
        g.drawLine(3, 3, 3 + (iconWidth - 7), 3);
        g.drawLine(4, 4, 4 + (iconWidth - 9), 4);
        g.translate(-x, -y);
    }

    @Override
    public int getIconWidth() {
        return 10;
    }

    @Override
    public int getIconHeight() {
        return 5;
    }
}