all: $(foreach file, $(basename $(wildcard *.tex)), $(file).pdf)

%.dvi: %.tex
	latex $*.tex

%.ps: %.dvi
	dvips -sPAPERSIZE=a4 $*.dvi -o

%.eps: %.ps
	ps2eps -f $*.ps

%.pdf: %.eps Makefile
	epstopdf $*.eps

clean:
	rm -f *.aux *.log *.dvi *.ps
