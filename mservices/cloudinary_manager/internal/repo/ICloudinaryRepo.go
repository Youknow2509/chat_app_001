package repo

// interfacr cloudinary 
type ICloudinary interface {
    // upload image to cloudinary
    // UploadImage(file File, publicID string) (string, error)
    // delete image from cloudinary
    DeleteImage(publicID string) error
    // get image from cloudinary by publicID
    GetImage(publicID string) (string, error)
}