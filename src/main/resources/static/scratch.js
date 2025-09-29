db.lead.aggregate([
    {
        $match: {
            assignedToId: ObjectId("689ccdf26d6fb66fb8b77d2f")
        }
    },
    {
        $lookup: {
            from: "users",
            let: {cid: "$assignedToId"},
            pipeline: [
                {$match: {$expr: {$eq: ["$_id", "$$cid"]}}},
                {$project: {username: 1, _id: 0}}
            ],
            as: "user"
        }
    }
])