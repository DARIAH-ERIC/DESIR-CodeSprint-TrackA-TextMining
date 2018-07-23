define(['jquery'], function($) {

  'use strict';

  /************************************************************
  @description Eigenes Accordion Script ermöglicht das gleichzeitige
  öffnen mehrerer Accordion-Einträge
  *************************************************************/
  var Tabs = {
    animation: {}, //animation timeout @see Tabs.start_animation
    skip_anim: 0, // skip first call of animation_start();
    _cacheElements: function() {
      this.$animatedTabs = $('.animated-tabs');

      this.$first_tab = $('.tab-content > div:first-child')
                          .attr('aria-expanded', 'true')
                          .attr('aria-hidden', 'false')
                          .addClass('current-tab');

      this.$tabpanels = $('.tab-content > div').
                          attr('role', 'tabpanel');

      this.$hidden_tabs = $('.tab-content > div').not('.tab-content > div:first-child')
                          .hide()
                          .attr('aria-expanded', 'false')
                          .attr('aria-hidden', 'true')
                          .attr('role', 'tab-panel');

      this.$tab_element = $('.tabs');
      this.$tab_links = $('.tab-nav h2, .tab-nav a').addClass('tab-header')
                                                    .attr('role', 'tab')
                                                    .attr('tabindex', '0')
                                                    .attr('aria-selected', 'false');

      this.$tab_nav = $('.tab-nav').attr('role', 'tablist');
      this.$first_tab_nav = $('.tab-nav > :first-child')
                              .addClass('current-tab-nav')
                              .removeAttr('tabindex')
                              .attr('aria-selected', 'true');

      this.$first_tab_nav.next().addClass('next-tab');
      this.$tabs = $('.tab-content > div');
      this.$toggle_animation_items = $('body');
    },
    init: function() {

      // Defaults
      this.autoplay_speed = 5000;
      this.fade_speed = 200;
      this.tab_number = 1;

      // Functions
      this.cacheElements();
      this.bindEvents();
      this.addARIAlabels();

      // Animation bei Tabs starten
      // $('.animated-tabs').mf_DoItIfNeeded(function(){ //
        Tabs.start_animation();
      // });

      // events
      this.$tab_element.each(function() {
        $(this).trigger('tabs.initialized');
      });

    },
    _bindEvents: function() {

      // mouse und enter events
      this.$tab_links.on('keydown', function(event) {
        if (event.keyCode === 13) {
          var $target_tab = $(this).attr('data-rel');
          Tabs.nextTab($(this).closest('.tabs'), $('#' + $target_tab), $(this));
        }
      });
      this.$tab_links.on('click', function(event) {
        event.preventDefault();
        var $target_tab = $(this).attr('data-rel');
        Tabs.nextTab($(this).closest('.tabs'), $('#' + $target_tab), $(this));
      });

      // Animation anhalten bei hover und bei Focus auf einem Tab
      $('.main-theme').mouseenter(function() {
        Tabs.stop_animation();
      });
      this.$tab_links.on('focus', function(event) {
        Tabs.stop_animation();
      });

      // Animation wieder abspielen
      $('.main-theme').on('mouseleave', function(event) {
        Tabs.skip_anim = 0; //Set timeout for next tab
        Tabs.start_animation();
      });

    },
    _addARIAlabels: function() {
      this.$tab_element.each(function(index) {

        var $tab_nav = $(this).find('> .tab-nav'),
            $tab_content = $(this).find('> .tab-content');

        $tab_nav.find('> h2, > a').each(function (index) {

          // set aria-controls
          index = index + 1;
          $(this).attr('aria-controls', $(this).closest('.tabs').find('> .tab-content > :nth-child('+ index +')').attr('id'));
        });

        $tab_content.find('> div').each(function (index) {

          // set aria-labelledby
          index = index + 1;
          $(this).attr('aria-labelledby', $(this).closest('.tabs').find('> .tab-nav > :nth-child('+ index +')').attr('id'));
        });

      });
    },
    _nextTab: function($tab_element, $target_tab, $target_tab_nav) {

      var $current_tab = $tab_element.find('> div > .current-tab'),
          $current_tab_nav = $tab_element.find('> div > .current-tab-nav'),
          tabs_amount = $tab_element.find('> .tab-content > div').length;

      if (!$target_tab) {
        // @description Setzt bei Autoplay die nächste Folie und den nächsten Navigationpunkt.
        // Setzt bei Klick den Zähler für das Autoplay auf den index des geklickten Tabs.

        if (Tabs.tab_number < tabs_amount) {
          // Ende noch nicht erreicht
          $target_tab = $current_tab.next();
          $target_tab_nav = $current_tab_nav.next();
          Tabs.tab_number = Tabs.tab_number + 1;

        } else {
          // Ende erreicht, beginne von vorn
          $target_tab = $tab_element.find('> .tab-content > div:first-child');
          $target_tab_nav = $tab_element.find('.tab-nav > .tab-header:first-child');
          Tabs.tab_number = 1;

        }

      } else {

        // @description Setzt bei Klick den Zähler für das Autoplay auf den index des geklickten Tabs.
        if($tab_element.hasClass('animated-tabs')) {
          Tabs.tab_number = $target_tab_nav.index() + 1;
        }
      }

      // @description Setzt 'current'-Status auf den nächsten Eintrag.
      $current_tab.fadeOut(this.fade_speed, function() {

        // tab inhalt status ändern
        $current_tab.removeClass('current-tab')
                    .attr('aria-expanded', 'false')
                    .attr('aria-hidden', 'true');

        // navi status ändern
        $current_tab_nav.removeClass('current-tab-nav')
                        .attr('tabindex', '0')
                        .attr('aria-selected', 'false')
                        .addClass('green');

        // neuen tab status ändern
        $target_tab.fadeIn(this.fade_speed, function() {
          // events
          $target_tab.trigger('tabs.opened', [$target_tab_nav, $target_tab]);
        })
         .attr('aria-expanded', 'true')
         .attr('aria-hidden', 'false')
         .addClass('current-tab');

        // prev next Klassen entfernen
        $current_tab_nav.prev().removeClass('prev-tab');
        $current_tab_nav.next().removeClass('next-tab');

        // aktuellen navi status ändern
        $target_tab_nav.addClass('current-tab-nav')
                      .removeAttr('tabindex')
                      .attr('aria-selected', 'true')
                      .removeClass('green');

        // prev next Klassen neu erstellen
        $target_tab_nav.next().addClass('next-tab');
        $target_tab_nav.prev().addClass('prev-tab');

        // Inhalte animieren/einblenden
        Tabs.animate_content($target_tab);

      });

    },
    _start_animation: function() {

      // initialen aufruf durch .animated-tabs verhindern
      if (Tabs.skip_anim > 0) {
        Tabs.$animatedTabs.each(function() {
          var that = $(this);

          Tabs.nextTab(that);

          that.trigger('tabs.animated', that);

        });
      }

      // timeout erstellen und skip erhöhen damit if ausgeführt wird
      Tabs.animation = setTimeout(Tabs.start_animation, Tabs.autoplay_speed);
      Tabs.skip_anim = Tabs.skip_anim + 1;



    },
    _stop_animation: function() {
      clearTimeout(Tabs.animation);
    },
    /************************************************************
      @description Inhalte im Tab animieren
    *************************************************************/
    _animate_content: function($target_tab) {
      $target_tab.children().hide().delay(0).fadeIn(200);
    }
  };

  return {
    init: Tabs.init
  };


});
