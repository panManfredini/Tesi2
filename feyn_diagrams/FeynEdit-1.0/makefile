SRC = .
BLD = build

PKGDIR = de/feynarts
SRCDIR = $(SRC)/$(PKGDIR)

SOURCES = \
  $(SRCDIR)/AbstractAction.java \
  $(SRCDIR)/AbstractCoordinateSystem.java \
  $(SRCDIR)/AbstractLinkable.java \
  $(SRCDIR)/AbstractUndoableEditListener.java \
  $(SRCDIR)/Addable.java \
  $(SRCDIR)/Addables.java \
  $(SRCDIR)/CannotMergeException.java \
  $(SRCDIR)/CompoundIterator.java \
  $(SRCDIR)/CoordinateSystem.java \
  $(SRCDIR)/Delete.java \
  $(SRCDIR)/Diagram.java \
  $(SRCDIR)/DiagramPanel.java \
  $(SRCDIR)/Document.java \
  $(SRCDIR)/DrawPropagator.java \
  $(SRCDIR)/DrawTadpole.java \
  $(SRCDIR)/Editor.java \
  $(SRCDIR)/EditorPanel.java \
  $(SRCDIR)/Entity.java \
  $(SRCDIR)/ExportImportReceiver.java \
  $(SRCDIR)/IntProperty.java \
  $(SRCDIR)/Label.java \
  $(SRCDIR)/Linkable.java \
  $(SRCDIR)/LinkedList.java \
  $(SRCDIR)/Propagator.java \
  $(SRCDIR)/Property.java \
  $(SRCDIR)/SelectAndMove.java \
  $(SRCDIR)/Separator.java \
  $(SRCDIR)/SimplePropagator.java \
  $(SRCDIR)/SquareCoordinateSystem.java \
  $(SRCDIR)/Tadpole.java \
  $(SRCDIR)/TeXPanel.java \
  $(SRCDIR)/Tool.java \
  $(SRCDIR)/UndoableEditListener.java \
  $(SRCDIR)/Vertex.java


JAKARTA = jakarta-regexp-1.5

#JFLAGS = -Xlint:unchecked

FeynEdit: FeynEdit.jar
	( echo "#! `which java` -jar" ; cat FeynEdit.jar ) > FeynEdit
	chmod 755 FeynEdit

FeynEdit.jar: $(SOURCES) Manifest.txt
	$(RM) -r $(BLD)
	mkdir $(BLD)
	CLASSPATH=$(SRCDIR):$(SRC)/$(JAKARTA): javac $(JFLAGS) -d $(BLD) $(SRCDIR)/Editor.java
	jar cfm FeynEdit.jar Manifest.txt \
	  -C $(BLD) de \
	  -C $(SRC)/$(JAKARTA) org \
	  -C $(SRC) $(PKGDIR)/icons
	$(RM) -r $(BLD)

VERSION = 1.0
TARDIR = FeynEdit-$(VERSION)
TARFILE = $(TARDIR).tar.gz

dist:
	$(RM) $(TARFILE)
	ln -s $(SRC) $(TARDIR)
	tar cvfhz $(TARFILE) $(TARDIR)/FeynEdit.jar \
	  $(TARDIR)/makefile $(TARDIR)/Manifest.txt \
	  $(TARDIR)/de $(TARDIR)/$(JAKARTA) \
	  $(TARDIR)/README $(TARDIR)/FeynEdit.pdf \
	  $(TARDIR)/feynarts.sty
	$(RM) $(TARDIR)

clean:
	$(RM) -r $(BLD)


.PHONY: dist clean

