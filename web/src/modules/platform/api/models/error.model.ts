export interface KatanError {
	readonly code: string | number
	readonly message: string
}

export class HttpError implements Error {
    message!: string
    name!: string

    readonly code!: string | number

    constructor(private readonly error: KatanError) {
        this.message = this.error.message
        this.name = `HttpError-${this.error.code}`
        this.code = this.error.code
    }
}

export const HttpErrors = {
    UNKNOWN_INSTANCE: 1003,
    UNKNOWN_BLUEPRINT: 1011
}
