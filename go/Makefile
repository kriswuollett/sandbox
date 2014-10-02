GOPATH = gopath
ORG_PATH = github.com/kriswuollett/sandbox
PKG = go

APPS = hello greetings

.PHONY : clean $(APPS)

all : $(APPS)

clean :
	rm -rf bin src

$(APPS) : %: bin/%

bin/% : %/main.go 
	rm -rf src 
	mkdir -p src/$(ORG_PATH)
	ln -s $(CURDIR) src/$(ORG_PATH)/$(PKG)
	$(eval $(call go env))
	env GOPATH=$(CURDIR) go build -o bin/$(notdir $@) $(ORG_PATH)/$(PKG)/$(notdir $@)

