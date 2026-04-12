(function(window) {
    'use strict';

    var carexPalette = [
        '#0f766e',
        '#f59e0b',
        '#8b5cf6',
        '#ef4444',
        '#06b6d4',
        '#84cc16',
        '#f97316',
        '#ec4899',
        '#14b8a6',
        '#6366f1',
        '#22c55e',
        '#eab308'
    ];

    window.configureCarexHighcharts = function(extraOptions) {
        if (typeof window.Highcharts === 'undefined') {
            return;
        }

        if (!window.__carexHighchartsThemeApplied) {
            window.Highcharts.setOptions({
                lang: { thousandsSep: ',' },
                credits: { enabled: false },
                colors: carexPalette
            });
            window.__carexHighchartsThemeApplied = true;
        }

        if (extraOptions) {
            window.Highcharts.setOptions(extraOptions);
        }
    };
})(window);
