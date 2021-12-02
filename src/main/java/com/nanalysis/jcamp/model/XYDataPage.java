/*
 * JCamp-Parser: a basic parsing library
 * Copyright (C) 2021 - Nanalysis Scientific Corp.
 * -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nanalysis.jcamp.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single XYDATA record as a page.
 * This may be an oversimplification, but is working to open benchtop data.
 */
public class XYDataPage extends JCampPage {
    public XYDataPage(JCampContainer parent) {
        super(parent);
    }

    @Override
    public String getHeader() {
        return get(Label.XYDATA).getString().lines().findFirst()
            .orElseThrow(() -> new IllegalStateException("Empty data header!"));
    }

    @Override
    public List<String> getDataLines() {
        return get(Label.XYDATA).getString().lines().skip(1)
            .collect(Collectors.toList());
    }

    @Override
    public String extractPageSymbol() {
        return "N"; // fake a single page, with N=1
    }

    @Override
    public String extractPageValue() {
        return "1"; // fake a single page, with N=1
    }

    @Override
    protected Form getFormForSymbol(String symbol) {
        // TODO ADSF support for XYDATA blocks - does this exist? then which parameter defines this form?
        return Form.AFFN; // default in case it is not specified
    }

    @Override
    protected int getDimensionForSymbol(String symbol) {
        return parent.getOrDefault(Label.NPOINTS, "0").getInt();
    }

    @Override
    protected double getFactorForSymbol(String symbol) {
        return parent.getOrDefault(symbol + "FACTOR", "1").getDouble();
    }
}
