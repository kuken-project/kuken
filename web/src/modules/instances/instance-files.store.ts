import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import { isNull, isUndefined, substringBeforeLast, systemPathSepatator } from "@/utils"
import { defineStore } from "pinia"
import { type Router, useRoute } from "vue-router"

type InstanceFilesStore = {
  dirRelativePath: string | null
  files: VirtualFile[]
  tree: VirtualFile[]
  expandedIds: Set<string>
  selectedId: string | null
}

export const useInstanceFilesStore = defineStore("instanceFiles", {
  state: (): InstanceFilesStore => ({
    dirRelativePath: null,
    files: [],
    tree: [],
    expandedIds: new Set(),
    selectedId: null
  }),
  getters: {
    getCurrentFilePath(): string {
      const filePath = useRoute().query?.filePath
      return (filePath || "") as string
    },

    getFiles(): VirtualFile[] {
      return this.files
    },

    getTree(): VirtualFile[] {
      return this.tree
    },

    getOpenedFiles(): VirtualFile[] {
      return this.files
    },

    getCurrentDirectoryRelativePath(): string {
      return this.dirRelativePath ?? ""
    }
  },
  actions: {
    setCurrentDirectory(path: string) {
      // Not a directory
      if (this.getCurrentFilePath === path) {
        this.dirRelativePath = substringBeforeLast(path, systemPathSepatator(path)!)
        this.initTree().then(() => this.select(path))
      } else {
        this.dirRelativePath = path
      }
    },

    async initTree() {
      if (this.tree.length !== 0) return

      const instance = useInstanceStore().getInstance.id
      return instancesService.listFiles(instance, "").then((files) => {
        this.tree = files

        if (!isNull(this.dirRelativePath)) {
          let path = this.dirRelativePath

          let cycles = 0
          do {
            this.expandedIds.add(path)

            const separator = systemPathSepatator(path)

            if (isUndefined(separator)) break

            path = substringBeforeLast(path, separator)
            cycles++

            if (cycles >= 5) {
              break
            }
          } while (true)
        }
      })
    },

    updateFiles(files: VirtualFile[]) {
      this.files = files
      this.initTree().then(() => {
        if (!isNull(this.dirRelativePath)) {
          this.toggleExpand(this.dirRelativePath)
        }
      })
    },

    toggleExpand(id: string): void {
      if (this.isExpanded(id)) {
        this.expandedIds.delete(id)
      } else {
        this.expandedIds.add(id)
      }
    },

    select(id: string): void {
      this.selectedId = id
    },

    isExpanded(id: string): boolean {
      return this.expandedIds.has(id)
    },

    goBack(router: Router) {
      const currentPath = this.getCurrentFilePath
      const pathSeparator = systemPathSepatator(currentPath)
      const isOnRoot = isUndefined(pathSeparator)
      if (isOnRoot) {
        router.replace({
          name: "instance.files"
        })
        return
      }

      const previousPath = substringBeforeLast(currentPath, pathSeparator)
      router.replace({
        name: "instance.files",
        query: {
          filePath: previousPath
        }
      })
    }
  }
})
