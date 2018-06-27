var question_answers1 = {
  q: "Welche Fähigkeiten möchten Sie im Sprachkurs vor allem lernen oder verbessern?",
  a: ['Lesen', 'Schreiben', 'Hören', 'Sprechen', 'allgemeiner Wortschatz', 'interkulturelle Aspekte']
};

var question_answers2 = {
    q: "In Persisch spricht man anders als was man schreibt. Welches Ziel am Anfang möchten Sie gerne erreichen?",
    a: ['sozialisieren', 'richtig Texte lesen und schreiben', 'Filme und Musik und so weiter verstehen']
};

var question_answers3 = {
    q: " Wie gut können Sie Persisch lesen/schreiben?" ,
    a: ['ein bisschen', 'nichts', 'gut', 'Pinglisch lesen/schreiben.' ]
};

var question_answers4 = {
    q: "Wie gut können Sie Persisch verstehen?",
    a: ['ein bisschen', 'nichts', 'gut' ]
};

var quesiton_answers5 = {
    q: "Wie gut können Sie Persisch sprechen?",
    a: ['ein bisschen', 'nichts', 'gut' ]
};

var quesiton_answers6 = {
    q: "Welche Themen in Persisch haben Sie bisher gemacht?",
    a: ['Verbkonjugation', 'Wortschatz und Aussprache', 'Grundwörter und Verben', 'Sprechen (Grundwörter)', 'Diskussion', 'Grammatik']
};

var quesiton_answers7 = {
    q: "Welche Themen finden Sie interessant zu lernen?",
    a: ['Alltag', 'Sport', 'Wissenschaft', 'Kultur (Essen, Musik, Film, ...)', 'Chat und Kommunikation', 'Geschichte', 'Geographie']
};

var qas = [ question_answers1, question_answers2, question_answers3, question_answers4, quesiton_answers5,quesiton_answers6, quesiton_answers7];

function gen_quesntion_answers_checkbox(htmldiv, qas) {
    var htmlText = "<label style='margin-left: 18px;'>Name:</label><input id='item0'><ol>";
    var cnt = 1;
    qas.forEach(function (qa) {
        htmlText += '<li>' + qa.q + "<br/><table><tr>";
        var answers = qa.a;
        answers.forEach(function (answer) {
            htmlText += '<td><input type="checkbox" id="item' + cnt + '"/><label for="item' + cnt + '">' + answer + '</label></td>\n';
            cnt++;
        });
        htmlText += '</tr></table><label>Andere Antwort: </label><input type="text" id="item' + cnt + '"><br/></li><br/><hr>\n';
        cnt++;
    });
    htmlText += "</ol>";
    htmldiv.innerHTML = htmlText;
    return cnt;
}

var count = gen_quesntion_answers_checkbox(document.getElementById("qa"), qas);

function fertig() {
    var res = [];
    for(var i=0;i <= count;i++) {
        var elem = document.getElementById("item" + i);
        if(elem != null) {
            if(elem.type == "text") {
                res.push(elem.value);
            } else {
                res.push(elem.checked);
            }
        }
    }


    $.get(serverAddr+'saveAnswers/' + JSON.stringify(res)).done(function (data) {
        alert("The anwers are saved.")
    }).fail(function (jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
    });
}