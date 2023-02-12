i head.inc
t <title>CFG Relais 1WIRE</title>
t <script language=JavaScript type="text/javascript" src="js/xml_http.js"></script>
t <script language=JavaScript>
t function cfgnom(carte) {
t window.open('web/rl1w'+carte+'cfg.cgi',"nom_popup","menubar=no, status=no, scrollbars=no,\
t menubar=no, width=300, height=410");}
t function ScanSav(f){
t if (f.WR[0].checked) {
t 	if(!confirm('Etes-vous sure de vouloir scanner les cartes relais ?')) return;
t  		f.submit();
t 	}
t else if (f.WR[1].checked) {
t 	if(!confirm('Etes-vous sure de vouloir enregistrer ?')) return;
t  		f.submit();
t 	}
t else if (f.WR[2].checked) {
t 	if(!confirm('Etes-vous sure de vouloir effacer cette carte relais ?')) return;
t  		f.submit();
t 	}
t }
t </script>
i pg_header.inc
t </style></head>
t <body>
i bandeau.inc
t <div id="cssmenu"><ul>
t <li class='active '><a href='index.cgi'><span>Accueil</span></a></li>
t <li class='has-sub '><a href='#'><span>Téléinfo</span></a>
t <ul><li><a href='TIC.cgi'><span>Valeurs Téléinfo</span></a></li>
c e n <li><a href='TICCPT1.cgi'><span>%s (CPT1)</span></a></li>
c e N <li><a href='TICCPT2.cgi'><span>%s (CPT2)</span></a></li>
t <li><a href='TICtreel.cgi'><span>Temps réel</span></a></li>
t  <li><a href='#'><span>Configuration</span></a>
t  <ul><li><a href='TICCFG.cgi'><span>Config. TIC</span></a></li>
t  <li><a href='TICINDEX.cgi'><span>Index compteurs</span></a></li>
t  <li><a href='TICGRAPH.cgi'><span>Config. Graphiques</span></a></li>
t </ul></li></ul>
t <li><a href='#'><span>Pince Amp.</span></a>
t <ul><li><a href='pceval.cgi'><span>Valeurs Pinces</span></a></li>
t <li><a href='pceA.cgi'><span>Graphes 1 & 2</span></a></li>
c GPA <li %s><a href='pceB.cgi'><span>Graphes 3 & 4</span></a></li>
t <li><a href='pcetreel.cgi'><span>Temps réel</span></a></li>
t <li><a href='pcecfg.cgi'><span>Configuration</span></a></li>
t </ul></li>
t <li><a href='#'><span>Cpt Pulses</span></a>
t <ul><li><a href='pulses.cgi'><span>Valeurs compteurs</span></a></li>
c GpA1 <li %s><a href='pulses1.cgi'><span>
c pn1 %s (pls1)</span></a></li>
c GpA2 <li %s><a href='pulses2.cgi'><span>
c pn2 %s (pls2)</span></a></li>
c GpA3 <li %s><a href='pulses3.cgi'><span>
c pn3 %s (pls3)</span></a></li>
c GpA4 <li %S><a href='pulses4.cgi'><span>
c pn4 %s (pls4)</span></a></li>
t <li><a href='plsCFG.cgi'><span>Configuration</span></a></li>
t </ul></li>
t <li><a href='#'><span>Température</span></a>
t <ul><li><a href='tmp.cgi'><span>Val. & Graphes</span></a></li>
t <li><a href='tmpmmax.cgi'><span>Temp. mini MAX</span></a></li>
t <li><a href='tmpcfg.cgi'><span>Configuration</span></a></li></ul></li>
t <li><a href='#'><span>Relais & Prog.</span></a>
t <ul><li><a href='relais.cgi'><span>Controle Relais</span></a></li>
t <li><a href='program.cgi'><span>Programmation</span></a></li>
t <li><a href='relcfg.cgi'><span>Configuration</span></a></li></ul></li>
t <li><a href='#'><span>Configuration</span></a>
t <ul><li><a href='horlcfg.cgi'><span>Horloge interne</span></a></li>
t  <li><a href='reseacfg.cgi'><span>Réseau</span></a></li>
t  <li><a href='accescfg.cgi'><span>Accès sécurisé</span></a></li>
t  <li><a href='emailcfg.cgi'><span>e-Mail</span></a></li>
t  <li><a href='M2Mcfg.cgi'><span>interface M2M</span></a></li>
t  <li><a href='lcdcfg.cgi'><span>Afficheur LCD</span></a></li>
t  <li><a href='infocfg.cgi'><span>Infos serveur</span></a></li>
t </ul></li></ul>
t </div>
t <form action=rel1wcfg.cgi method=post name=cgi>
t <div id="zone_affichage">
t <h2 align=center>Relais 1 WIRE</h2>
t <table class="none" align=center>
c WRn <h3 align=center>%d carte(s) relais détectée(s)</h3>
t <colgroup>
t <col span="1" width="190" style="background-color:#B8C7D3" />
t <col span="1" width="190" style="background-color:#CCCCCC" />
t <col span="1" width="110" style="background-color:#CCCCCC" />
t <col span="1" width="110" style="background-color:#CCCCCC" />
t <col span="2" width="60" style="background-color:#CCCCCC" />
t </colgroup>
t <tr><th>Carte(s)</th><th>Identifiant</th><th>Nom</th><th>NB de relais</th>
t <th>Détecté</th><th>Noms relais</th></tr>
c WRS0<tr><td>1ère (relais 101 à 108)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN0<td><input type=text name=WRN0 value="%s" size=15 maxlength=15></td>
c WRT0<td><center><input type="radio" name="WRT0" value="3" %s>3<input type="radio" name="WRT0" value="8" %s>8</td>
c WRD0<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(0)">Cfg</td></tr>
c WRS1<tr><td>2eme (relais 111 à 118)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN1<td><input type=text name=WRN1 value="%s" size=15 maxlength=15></td>
c WRT1<td><center><input type="radio" name="WRT1" value="3" %s>3<input type="radio" name="WRT1" value="8" %s>8</td>
c WRD1<td><center><img src="images/%s"></td><td align=center><button onclick="cfgnom(1)">Cfg</td></tr>
c WRS2<tr><td>3eme (relais 121 à 128)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN2<td><input type=text name=WRN2 value="%s" size=15 maxlength=15></td>
c WRT2<td><center><input type="radio" name="WRT2" value="3" %s>3<input type="radio" name="WRT2" value="8" %s>8</td>
c WRD2<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(2)">Cfg</td></tr>
c WRS3<tr><td>4eme (relais 131 à 138)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN3<td><input type=text name=WRN3 value="%s" size=15 maxlength=15></td>
c WRT3<td><center><input type="radio" name="WRT3" value="3" %s>3<input type="radio" name="WRT3" value="8" %s>8</td>
c WRD3<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(3)">Cfg</td></tr>
c WRS4<tr><td>5eme (relais 141 à 148)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN4<td><input type=text name=WRN4 value="%s" size=15 maxlength=15></td>
c WRT4<td><center><input type="radio" name="WRT4" value="3" %s>3<input type="radio" name="WRT4" value="8" %s>8</td>
c WRD4<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(4)">Cfg</td></tr>
c WRS5<tr><td>6eme (relais 151 à 158)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN5<td><input type=text name=WRN5 value="%s" size=15 maxlength=15></td>
c WRT5<td><center><input type="radio" name="WRT5" value="3" %s>3<input type="radio" name="WRT5" value="8" %s>8</td>
c WRD5<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(5)">Cfg</td></tr>
c WRS6<tr><td>7eme (relais 161 à 168)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN6<td><input type=text name=WRN6 value="%s" size=15 maxlength=15></td>
c WRT6<td><center><input type="radio" name="WRT6" value="3" %s>3<input type="radio" name="WRT6" value="8" %s>8</td>
c WRD6<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(6)">Cfg</td></tr>
c WRS7<tr><td>8eme (relais 171 à 178)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN7<td><input type=text name=WRN7 value="%s" size=15 maxlength=15></td>
c WRT7<td><center><input type="radio" name="WRT7" value="3" %s>3<input type="radio" name="WRT7" value="8" %s>8</td>
c WRD7<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(7)">Cfg</td></tr>
c WRS8<tr><td>9eme (relais 181 à 188)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN8<td><input type=text name=WRN8 value="%s" size=15 maxlength=15></td>
c WRT8<td><center><input type="radio" name="WRT8" value="3" %s>3<input type="radio" name="WRT8" value="8" %s>8</td>
c WRD8<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(8)">Cfg</td></tr>
c WRS9<tr><td>10eme (relais 191 à 198)</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c WRN9<td><input type=text name=WRN9 value="%s" size=15 maxlength=15></td>
c WRT9<td><center><input type="radio" name="WRT9" value="3" %s>3<input type="radio" name="WRT9" value="8" %s>8</td>
c WRD9<td><center><img src="images/%S"></td><td align=center><button onclick="cfgnom(9)">Cfg</td></tr>
t </table><br></div>
t <div id="zone_graphique">
t <center>
t <input type="radio" name="WR" value="sca" checked>Scanner les cartes
t <input type="radio" name="WR" value="sav">Sauvegarder
t <input type="radio" name="WR" value="efs">Effacer Carte N°
t <input type=txt1W size=2 maxlength=2 name=DW><br>
t <input type=button value="&nbsp;&nbsp;OK&nbsp;&nbsp;" name=wire onclick="ScanSav(this.form)"></center>
t </center>
t </div></form>
i pg_footer.inc
.