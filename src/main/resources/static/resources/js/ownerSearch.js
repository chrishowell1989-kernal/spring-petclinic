/*
 * Live, client-side name filter for the Find Owners page.
 *
 * As the user types in the search box, owner rows whose name (first + last)
 * does not contain the query are hidden. Matching is case-insensitive and uses
 * a substring (partial) match. When the box is empty the full list is shown;
 * when nothing matches a "no results" message is displayed instead.
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var input = document.getElementById('owner-name-search');
    var body = document.getElementById('owners-list-body');
    var noResults = document.getElementById('owners-no-results');

    if (!input || !body) {
      return;
    }

    var rows = Array.prototype.slice.call(body.getElementsByTagName('tr'));

    function ownerName(row) {
      var link = row.querySelector('td a');
      var text = link ? link.textContent : row.textContent;
      return text.trim().toLowerCase();
    }

    function applyFilter() {
      var query = input.value.trim().toLowerCase();
      var visible = 0;

      rows.forEach(function (row) {
        var matches = query === '' || ownerName(row).indexOf(query) !== -1;
        row.style.display = matches ? '' : 'none';
        if (matches) {
          visible += 1;
        }
      });

      if (noResults) {
        noResults.style.display = query !== '' && visible === 0 ? '' : 'none';
      }
    }

    input.addEventListener('input', applyFilter);
  });
})();
