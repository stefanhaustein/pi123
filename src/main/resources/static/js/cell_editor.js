import {currentSheet, currentCellId, currentCellData, currentCellElement, currentCellSavedFormula, selectCell} from "./model.js";
import {nullToEmtpy} from "./lib/util.js";

//document.getElementById("tableViewport").addEventListener("keydown", tableKeyPress)
//document.getElementById("table").addEventListener("keydown", tableKeyPress)

let inputElement = document.getElementById("current")

inputElement.addEventListener("change", sendInput)
inputElement.addEventListener("input", processInput)
inputElement.addEventListener("keydown", (event) => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        inputElement.blur()
        sendInput()
        selectCell(currentCellId, false)
    } else if (event.key == "Escape") {
        inputElement.value = nullToEmtpy(currentCellSavedFormula)
        inputElement.blur()
        sendInput()
        selectCell(currentCellId, false)
    } else {
        console.log(event.key)
        processInput()
    }
})
inputElement.addEventListener("focus", () => {
  selectCell(currentCellId, true)
})
/*
inputElement.addEventListener("blur", () => {
    currentCellElement.classList.remove("editing")
    currentCellElement.innerText = nullToEmtpy(currentCellData["c"])
})*/

selectCell("A1")

function sendInput() {
    processInput()
    console.log("sendInput")
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "update/" + currentSheet.name + "!" + currentCellId, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(currentSheet.cells[currentCellId]["f"]);
    console.log("xhr", xhr)
}

function processInput() {
    console.log("processInput")
    let value = inputElement.value
    currentCellData["f"] = value
    currentCellData["c"] = null
    currentCellElement.textContent = value
}



