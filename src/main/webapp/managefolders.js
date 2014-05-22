function selectAll() {
    var checkboxes = document.getElementsByTagName("input");
    
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].type == "checkbox") {
            checkboxes[i].checked = !checkboxes[i].checked;
        }
    }
}