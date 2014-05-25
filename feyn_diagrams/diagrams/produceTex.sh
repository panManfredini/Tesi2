#!bash
name=$1

printf "\\documentclass[12pt]{article} \n \\\usepackage{feynarts} \n \\\begin{document} \n \\\thispagestyle{empty}
	\n \\\begin{feynartspicture}(200,200)(1,1) \n \\\FADiagram{} \n 
	\n \n \\\end{feynartspicture} \n \n\\\end{document} " > "$1.tex" 
