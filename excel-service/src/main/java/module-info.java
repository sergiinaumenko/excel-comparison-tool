module ua.kh.excel {
    requires static lombok;
    requires org.apache.commons.lang3;
    requires commons.math3;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens ua.kh.excel.service to ua.kh.excel.comparison.desktop;
    opens ua.kh.excel.utils to ua.kh.excel.comparison.desktop;
    opens ua.kh.excel.provider to ua.kh.excel.comparison.desktop;
    opens ua.kh.excel.provider.impl to ua.kh.excel.comparison.desktop;

    exports ua.kh.excel.service;
    exports ua.kh.excel.utils;
    exports ua.kh.excel.provider;
    exports ua.kh.excel.provider.impl;
}