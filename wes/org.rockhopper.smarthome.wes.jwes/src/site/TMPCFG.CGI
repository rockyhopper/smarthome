i head.inc
t <title>Sondes 1WIRE 1 a 10</title>
t <script language=JavaScript type="text/javascript" src="js/xml_http.js"></script>
t <script language=JavaScript>
t function ScanSav(f){
t if (f.W1[0].checked) {
t 	if(!confirm('Etes-vous sure de vouloir scanner les sondes de Température ?')) return;
t  		f.submit();
t 	}
t else if (f.W1[1].checked) {
t 	if(!confirm('Etes-vous sure de vouloir enregistrer les sondes de Température ?')) return;
t  		f.submit();
t 	}
t else if (f.W1[2].checked) {
t 	if(!confirm('Etes-vous sure de vouloir effacer cette sondes de Température ?')) return;
t  		f.submit();
t 	}
t else if (f.W1[3].checked) {
t 	var cal = new Array(); var cde='';
t 	if(!confirm('Etes-vous sure de vouloir corriger ces valeurs ?')) return;
t	for(i=1;i<11;i++){
t	cal[i] = document.getElementById('tmp'+i+'_cal').value-document.getElementById('tmp'+i+'_val').value;
t 	cde += "wcal"+i+"="+cal[i].toFixed(2);
t	if (i<10) cde +="&"}
t	AJAXCommand("?"+cde);
t 	}
t }
t var formUpdate = new periodicObj("tmpcfg.cgx", 30000);
t setInterval(function() {updateMultiple(formUpdate);}, 30000);
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
t <form action=tmpcfg.cgi method=post name=cgi>
t <div id="zone_affichage">
t <h2 align=center>Configuration sonde(s) 1 wire (Température / Humidité)</h2>
t <table class="none" align=center>
t <colgroup>
t <col span="1" width="250" style="background-color:#B8C7D3" />
t <col span="2" width="150" style="background-color:#CCCCCC" />
t </colgroup>
t <tr><td align="center">Activer lecture des sondes</td>
c G w <td><input type="radio" name="OWACT" value="ON" %s>ON<input type="radio" name="OWACT" value="OFF" %s>
t OFF</td></tr>
t <tr><td align="center">Activer fichier CSV (1enr/minute)</td>
c G W <td><input type="radio" name="OWCSV" value="ON" %s>ON<input type="radio" name="OWCSV" value="OFF" %s>
t OFF</td></tr></table>
t <table class="none" align=center>
c W Q <h3 align=center>%d sonde(s) de température
c W H & %d sonde(s) d'humidité détectée(s)</h3>
t <colgroup>
t <col span="1" width="45" style="background-color:#B8C7D3" />
t <col span="1" width="225" style="background-color:#CCCCCC" />
t <col span="1" width="100" style="background-color:#CCCCCC" />
t <col span="1" width="95" style="background-color:#CCCCCC" />
t <col span="2" width="100" style="background-color:#CCCCCC" />
t <col span="2" width="55" style="background-color:#CCCCCC" />
t </colgroup>
t <tr><th>N°</th><th>Identifiant</th><th>Nom</th><th>Type</th>
t <th>Valeurs lues</th><th>valeurs corrigées</th><th>Détecté</th><th>Graph.<br>1 à 4</th></tr>
c W0S0<tr><td align=center>1</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N0<td><input type=text name=TW00 value="%s" size=15 maxlength=15></td>
c W0t0<td>%s</td>
c W0C0<td><input type="txtxml" style="width:65px;" readonly id="tmp1_val" value="%.01f"> %s</td>
c W0T0<td><input type="txtD" id="tmp1_cal" value="%.01f"> %s</td>
c W0D0<td><center><img src="images/%s"></center></td>
c W0G0<td><center><input type=txt1W name=WG00 value="%d" size=2 maxlength=2></center></td></tr>
c W0S1<tr><td align=center>2</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N1<td><input type=text name=TW01 value="%s" size=15 maxlength=15></td>
c W0t1<td>%s</td>
c W0C1<td><input type="txtxml" style="width:65px;" readonly id="tmp2_val" value="%.01f"> %s</td>
c W0T1<td><input type="txtD" id="tmp2_cal" value="%.01f"> %s</td>
c W0D1<td><center><img src="images/%s"></center></td>
c W0G1<td><center><input type=txt1W name=WG01 value="%d" size=2 maxlength=2></center></td></tr>
c W0S2<tr><td align=center>3</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N2<td><input type=text name=TW02 value="%s" size=15 maxlength=15></td>
c W0t2<td>%s</td>
c W0C2<td><input type="txtxml" style="width:65px;" readonly id="tmp3_val" value="%.01f"> %s</td>
c W0T2<td><input type="txtD" id="tmp3_cal" value="%.01f"> %s</td>
c W0D2<td><center><img src="images/%s"></center></td>
c W0G2<td><center><input type=txt1W name=WG02 value="%d" size=2 maxlength=2></center></td></tr>
c W0S3<tr><td align=center>4</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N3<td><input type=text name=TW03 value="%s" size=15 maxlength=15></td>
c W0t3<td>%s</td>
c W0C3<td><input type="txtxml" style="width:65px;" readonly id="tmp4_val" value="%.01f"> %s</td>
c W0T3<td><input type="txtD" id="tmp4_cal" value="%.01f"> %s</td>
c W0D3<td><center><img src="images/%s"></center></td>
c W0G3<td><center><input type=txt1W name=WG03 value="%d" size=2 maxlength=2></center></td></tr>
c W0S4<tr><td align=center>5</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N4<td><input type=text name=TW04 value="%s" size=15 maxlength=15></td>
c W0t4<td>%s</td>
c W0C4<td><input type="txtxml" style="width:65px;" readonly id="tmp5_val" value="%.01f"> %s</td>
c W0T4<td><input type="txtD" id="tmp5_cal" value="%.01f"> %s</td>
c W0D4<td><center><img src="images/%s"></center></td>
c W0G4<td><center><input type=txt1W name=WG04 value="%d" size=2 maxlength=2></center></td></tr>
c W0S5<tr><td align=center>6</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N5<td><input type=text name=TW05 value="%s" size=15 maxlength=15></td>
c W0t5<td>%s</td>
c W0C5<td><input type="txtxml" style="width:65px;" readonly id="tmp6_val" value="%.01f"> %s</td>
c W0T5<td><input type="txtD" id="tmp6_cal" value="%.01f"> %s</td>
c W0D5<td><center><img src="images/%s"></center></td>
c W0G5<td><center><input type=txt1W name=WG05 value="%d" size=2 maxlength=2></center></td></tr>
c W0S6<tr><td align=center>7</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N6<td><input type=text name=TW06 value="%s" size=15 maxlength=15></td>
c W0t6<td>%s</td>
c W0C6<td><input type="txtxml" style="width:65px;" readonly id="tmp7_val" value="%.01f"> %s</td>
c W0T6<td><input type="txtD" id="tmp7_cal" value="%.01f"> %s</td>
c W0D6<td><center><img src="images/%s"></center></td>
c W0G6<td><center><input type=txt1W name=WG06 value="%d" size=2 maxlength=2></center></td></tr>
c W0S7<tr><td align=center>8</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N7<td><input type=text name=TW07 value="%s" size=15 maxlength=15></td>
c W0t7<td>%s</td>
c W0C7<td><input type="txtxml" style="width:65px;" readonly id="tmp8_val" value="%.01f"> %s</td>
c W0T7<td><input type="txtD" id="tmp8_cal" value="%.01f"> %s</td>
c W0D7<td><center><img src="images/%s"></center></td>
c W0G7<td><center><input type=txt1W name=WG07 value="%d" size=2 maxlength=2></center></td></tr>
c W0S8<tr><td align=center>9</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N8<td><input type=text name=TW08 value="%s" size=15 maxlength=15></td>
c W0t8<td>%s</td>
c W0C8<td><input type="txtxml" style="width:65px;" readonly id="tmp9_val" value="%.01f"> %s</td>
c W0T8<td><input type="txtD" id="tmp9_cal" value="%.01f"> %s</td>
c W0D8<td><center><img src="images/%s"></center></td>
c W0G8<td><center><input type=txt1W name=WG08 value="%d" size=2 maxlength=2></center></td></tr>
c W0S9<tr><td align=center>10</td><td>%02X %02X %02X %02X %02X %02X %02X %02X</td>
c W0N9<td><input type=text name=TW09 value="%s" size=15 maxlength=15></td>
c W0t9<td>%s</td>
c W0C9<td><input type="txtxml" style="width:65px;" readonly id="tmp10_val" value="%.01f"> %s</td>
c W0T9<td><input type="txtD" id="tmp10_cal" value="%.01f"> %s</td>
c W0D9<td><center><img src="images/%s"></center></td>
c W0G9<td><center><input type=txt1W name=WG09 value="%d" size=2 maxlength=2></center></td></tr>
t </table><br><center>
t <input type=button value="Capteurs de 1 à 10" name=wire onclick="window.location='tmpcfg.cgi'">
t <input type=button value="Capteurs de 11 à 20" name=wire onclick="window.location='tmpcfg2.cgi'">
t <input type=button value="Capteurs de 21 à 30" name=wire onclick="window.location='tmpcfg3.cgi'">
t </center></div>
t <div id="zone_graphique"><br>
t <center><input type="radio" name="W1" value="sca" checked>Scanner les sondes
t <input type="radio" name="W1" value="sav">Sauvegarder
t <input type="radio" name="W1" value="efs">Effacer Sonde N°
t <input type=txt1W size=2 maxlength=2 name=DW>
t <input type="radio" name="W1" value="eta">Corriger valeurs<br>
t <input type=button value="&nbsp;&nbsp;OK&nbsp;&nbsp;" name=wire onclick="ScanSav(this.form)"></center>
t </div></form>
i pg_footer.inc
.
