Das Programm akzeptiert ein bis mehrere Dateien als Eingaben pro Seite, dabei ist es wichtig,
dass in der Hauptdatei eine Klasse existiert, die dem Dateinamen entspricht.
Programme müssen gewisse Bedingungen erfüllen um erfolgreich verwoben zu werden:
-Methoden mit Rückgabewert haben nur eine return Anweisung am Ende ihres Rumpfes
-rekursive Methoden deren Aufrufe verwoben werden sollen, müssen Teil der Hauptklasse sein
-interface, abstract kann nicht verwoben werden
-im Falle nicht automatischer Methodenverwebung müssen die zu verwebenden Aufrufe mit //@methodWeave annotiert werden
-bei mehreren Methodenaufrufen innerhalb z.B. einer Zuweisung werden jeweils die ersten, die auch innerhalb des Programmcodes definiert wurden, miteinander verwoben
-Methoden, Variablen und Klassen die definiert werden müssen alle einen einzigartigen Namen haben, der auch mit keiner
 externen Methode, Variable oder Klasse die aufgerufen wird übereinstimmen, darf
-Zuweisungen oder Aufrufe der Form: a = this; oder m(this), werden nicht unterstützt
-Exceptions dürfen nur in try-catch-Blöcken oder nicht zu verwebenden Methoden (auch nicht über
 die Methodenaufrufsverwebung) auftreten, sonst ist das PP im Allgemeinen nicht korrekt

Annotationen:
Annotationen sind von folgender Form: //@weaveAnno<Typ>[label]
'Typ' muss ersetzt werden durch den entsprechenden Namen, im Moment gibt es nur 'default'. Das 'label' muss an Anfang und Endannotation übereinstimmen.
In diesem Rahmen muss das 'label' einzigartig sein. 'Typ' und 'label' müssen mit den zu verwebenden Annotationen des zweiten Programms übereinstimmen
Damit sie funktionieren müssen sie an den Anfang des Kommentarblocks von etwas geschrieben sein, z.B.:
//@weaveAnno<default>[label]
//andere kommentare
int a = foo();
  ....weiterer Code

//@weaveAnno[label]
int b;

Hierbei dient die zweite Annotation als Markierung für den Endpunkt des zu verwebenden Blocks,
dabei wird das Objekt an dem die Annotation hängt NICHT mit verwoben, hier also 'int b;'

default ruft einfach die normale Verwebung wieder auf auf den markierten Teilblücken, nützlich falls man z.B. eine while-Schleife überspringen möchte oder Ähnliches.

Abkürzen des Eingabevorgangs über eine Eingabedatei:
Bei der Interaktion kann eine Eingabedatei gespeichert werden, den Pfad zu dieser kann man entweder
im Eingabedialog übergeben oder als Parameter in der Kommandozeile beim Aufruf der .jar
