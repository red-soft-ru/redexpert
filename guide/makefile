PDFS=RedExpert_Guide-ru.pdf
ROOT=$(PWD)

.PHONY: all pdfs clean

all: pdfs

include make.rules

pdfs: $(PDFS)

clean:
	$(foreach ext, $(CLEAN_FILES),rm -f *.$(ext);)

package: all prepare_dirs
	$(foreach f, $(PDFS),cp $(f) $(ROOT)/out;)