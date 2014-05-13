var itemsArray = [];

function addItem(item) {
    itemsArray.push(item);
}

function addGroupRow() {
    addRow("GROUP");
}

function addUserRow() {
    addRow("USER");
}

function addRow(type) {
    var id = document.getElementById('addUserGroupText').value;
        
    if (!id) {
        return alert("Please enter a username or a group name");
    }
    
    var table = document.getElementById('permissionTable');
    
    var row = table.insertRow(-1);
    row.setAttribute('name', type + ':' + id);
    
    var idCell = row.insertCell(-1);
    idCell.innerHTML = id;
    
    for (var i = 0; i < itemsArray.length; i++) {
        var cell = row.insertCell(-1);
        cell.innerHTML = '<input class="" type="checkbox" name="[' + itemsArray[i] +']"></input>';
    }
}